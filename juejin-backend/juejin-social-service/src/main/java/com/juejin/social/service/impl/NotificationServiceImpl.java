package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.entity.Notification;
import com.juejin.social.mapper.NotificationMapper;
import com.juejin.social.service.NotificationService;
import com.juejin.social.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserFeignClient userFeignClient;

    @Override
    public PageResult<NotificationVO> getNotificationList(Long userId, Integer page, Integer size, String type) {
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        notificationMapper.selectPage(pageParam, wrapper);

        List<NotificationVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(Long userId, String type, String title, String content,
                                   Long targetId, String targetType, Long senderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        save(notification);

        log.debug("Notification created: userId={}, type={}, senderId={}", userId, type, senderId);
    }

    /**
     * 通知实体转VO，含发送者用户信息
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setTargetId(notification.getTargetId());
        vo.setTargetType(notification.getTargetType());
        vo.setSenderId(notification.getSenderId());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());

        // 填充发送者信息
        if (notification.getSenderId() != null) {
            fillSenderInfo(vo, notification.getSenderId());
        }

        return vo;
    }

    private void fillSenderInfo(NotificationVO vo, Long senderId) {
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(senderId);
            if (result != null && result.getData() != null) {
                vo.setSenderNickname(result.getData().getNickname());
                vo.setSenderAvatar(result.getData().getAvatar());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch sender info: {}", e.getMessage());
        }
    }

}
