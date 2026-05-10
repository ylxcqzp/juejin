package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.entity.Conversation;
import com.juejin.social.mapper.ConversationMapper;
import com.juejin.social.service.ConversationService;
import com.juejin.social.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话 Service 实现
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final UserFeignClient userFeignClient;

    @Override
    public PageResult<ConversationVO> listConversations(Long userId, int page, int size) {
        List<Conversation> all = conversationMapper.selectByUserId(userId);
        List<ConversationVO> vos = new ArrayList<>();
        for (Conversation conv : all) {
            ConversationVO vo = buildConversationVO(conv, userId);
            if (vo != null) vos.add(vo);
        }
        // 内存分页
        long total = vos.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, (int) total);
        if (from >= total) return PageResult.of(List.of(), total, page, size);
        return PageResult.of(vos.subList(from, to), total, page, size);
    }

    @Override
    public ConversationVO getOrCreateConversation(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new com.juejin.common.exception.BusinessException(
                    com.juejin.common.result.ErrorCode.BAD_REQUEST.getCode(), "不能和自己创建会话");
        }
        // 确保 user1_id < user2_id
        Long u1 = Math.min(userId, otherUserId);
        Long u2 = Math.max(userId, otherUserId);
        Conversation conv = lambdaQuery()
                .eq(Conversation::getUser1Id, u1)
                .eq(Conversation::getUser2Id, u2)
                .one();
        if (conv == null) {
            conv = new Conversation();
            conv.setUser1Id(u1);
            conv.setUser2Id(u2);
            conv.setUser1Unread(0);
            conv.setUser2Unread(0);
            conv.setLastMessageTime(LocalDateTime.now());
            save(conv);
        }
        return buildConversationVO(conv, userId);
    }

    private ConversationVO buildConversationVO(Conversation conv, Long userId) {
        Long otherUserId = conv.getUser1Id().equals(userId) ? conv.getUser2Id() : conv.getUser1Id();
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setOtherUserId(otherUserId);
        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setUnreadCount(conv.getUser1Id().equals(userId) ? conv.getUser1Unread() : conv.getUser2Unread());
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(otherUserId);
            if (result != null && result.getData() != null) {
                vo.setOtherUserNickname(result.getData().getNickname());
                vo.setOtherUserAvatar(result.getData().getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败 userId={}", otherUserId, e);
            vo.setOtherUserNickname("用户" + otherUserId);
        }
        return vo;
    }
}
