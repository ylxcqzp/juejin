package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知Mapper
 *
 * @author juejin
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 统计未读通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    long countUnread(@Param("userId") Long userId);

    /**
     * 标记所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() " +
            "WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    void markAllAsRead(@Param("userId") Long userId);

}
