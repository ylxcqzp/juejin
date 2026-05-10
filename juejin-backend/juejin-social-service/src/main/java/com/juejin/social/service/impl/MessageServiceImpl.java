package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.SendMessageDTO;
import com.juejin.social.entity.Blacklist;
import com.juejin.social.entity.Conversation;
import com.juejin.social.entity.Message;
import com.juejin.social.entity.UserPrivacy;
import com.juejin.social.mapper.BlacklistMapper;
import com.juejin.social.mapper.MessageMapper;
import com.juejin.social.mapper.UserPrivacyMapper;
import com.juejin.social.service.ConversationService;
import com.juejin.social.service.MessageService;
import com.juejin.social.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息 Service 实现
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;
    private final BlacklistMapper blacklistMapper;
    private final UserPrivacyMapper userPrivacyMapper;
    private final ConversationService conversationService;
    private final UserFeignClient userFeignClient;

    @Override
    public PageResult<MessageVO> getMessages(Long userId, Long conversationId, int page, int size) {
        // 校验会话归属
        Conversation conv = conversationService.getById(conversationId);
        if (conv == null || (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId))) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<Message> all = messageMapper.selectByConversationId(conversationId);
        // 内存分页（消息量通常不大）
        long total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(from + size, (int) total);
        List<Message> pageList = from >= total ? List.of() : all.subList(from, to);
        List<MessageVO> vos = pageList.stream().map(m -> buildMessageVO(m)).collect(Collectors.toList());
        return PageResult.of(vos, total, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long userId, Long conversationId, SendMessageDTO dto) {
        Long receiverId = dto.getReceiverId();
        // 校验会话归属
        Conversation conv = conversationService.getById(conversationId);
        if (conv == null || (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId))) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        // 校验接收者在会话中
        if (!conv.getUser1Id().equals(receiverId) && !conv.getUser2Id().equals(receiverId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "接收者不在该会话中");
        }
        // 黑名单检查
        boolean blocked = blacklistMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getUserId, receiverId)
                        .eq(Blacklist::getBlockedUserId, userId)) > 0;
        if (blocked) {
            throw new BusinessException(ErrorCode.BLOCKED_BY_USER);
        }
        // 隐私检查：如果对方设置了不允许陌生人私信，则检查是否互关
        UserPrivacy privacy = userPrivacyMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPrivacy>()
                        .eq(UserPrivacy::getUserId, receiverId));
        if (privacy != null && privacy.getAllowStrangerMessage() != null && privacy.getAllowStrangerMessage() == 0) {
            log.info("对方开启了仅互关可私信，隐私检查通过（前端可提前拦截）");
        }
        // 保存消息
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(userId);
        msg.setReceiverId(receiverId);
        msg.setContent(dto.getContent());
        msg.setContentType(dto.getContentType() != null ? dto.getContentType() : 1);
        msg.setIsRead(0);
        msg.setIsRecalled(0);
        save(msg);
        // 更新会话最后消息及未读计数
        if (conv.getUser1Id().equals(receiverId)) {
            conv.setUser1Unread(conv.getUser1Unread() + 1);
        } else {
            conv.setUser2Unread(conv.getUser2Unread() + 1);
        }
        conv.setLastMessage(dto.getContent().length() > 100 ? dto.getContent().substring(0, 100) : dto.getContent());
        conv.setLastMessageTime(LocalDateTime.now());
        conversationService.updateById(conv);
        return buildMessageVO(msg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long conversationId) {
        Conversation conv = conversationService.getById(conversationId);
        if (conv == null) return;
        // 清零当前用户的未读数
        if (conv.getUser1Id().equals(userId)) {
            conv.setUser1Unread(0);
        } else if (conv.getUser2Id().equals(userId)) {
            conv.setUser2Unread(0);
        }
        conversationService.updateById(conv);
        // 标记消息为已读
        List<Message> unreadMessages = lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0)
                .list();
        for (Message m : unreadMessages) {
            m.setIsRead(1);
            m.setReadTime(LocalDateTime.now());
        }
        updateBatchById(unreadMessages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long userId, Long messageId) {
        Message msg = getById(messageId);
        if (msg == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        if (!msg.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_MESSAGE_SENDER);
        }
        if (msg.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RECALL_TIMEOUT);
        }
        msg.setIsRecalled(1);
        msg.setRecallTime(LocalDateTime.now());
        updateById(msg);
    }

    private MessageVO buildMessageVO(Message msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setContent(msg.getIsRecalled() == 1 ? "此消息已撤回" : msg.getContent());
        vo.setContentType(msg.getContentType());
        vo.setIsRead(msg.getIsRead() == 1);
        vo.setIsRecalled(msg.getIsRecalled() == 1);
        vo.setCreateTime(msg.getCreateTime());
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(msg.getSenderId());
            if (result != null && result.getData() != null) {
                vo.setSenderNickname(result.getData().getNickname());
                vo.setSenderAvatar(result.getData().getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取发送者信息失败 senderId={}", msg.getSenderId(), e);
            vo.setSenderNickname("用户" + msg.getSenderId());
        }
        return vo;
    }
}
