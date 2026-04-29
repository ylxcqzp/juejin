package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.social.entity.Notification;
import com.juejin.social.vo.NotificationVO;

/**
 * 消息通知Service接口
 *
 * @author juejin
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 分页查询用户通知列表
     */
    PageResult<NotificationVO> getNotificationList(Long userId, Integer page, Integer size, String type);

    /**
     * 获取未读通知数
     */
    long getUnreadCount(Long userId);

    /**
     * 标记全部已读
     */
    boolean markAllAsRead(Long userId);

    /**
     * 创建通知（内部调用，由点赞/评论/关注等事件触发）
     */
    void createNotification(Long userId, String type, String title, String content,
                           Long targetId, String targetType, Long senderId);

}
