package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 接收用户ID */
    private Long userId;

    /** 通知类型：system/like/comment/follow/audit */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联目标ID */
    private Long targetId;

    /** 关联目标类型 */
    private String targetType;

    /** 发送者ID */
    private Long senderId;

    /** 是否已读：0-未读 1-已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

}
