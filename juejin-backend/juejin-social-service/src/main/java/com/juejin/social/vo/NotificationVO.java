package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知VO
 *
 * @author juejin
 */
@Data
@Schema(description = "消息通知")
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "通知ID", example = "100")
    private Long id;

    @Schema(description = "通知类型：system/like/comment/follow/audit", example = "like")
    private String type;

    @Schema(description = "通知标题", example = "你的文章收到新的点赞")
    private String title;

    @Schema(description = "通知内容", example = "张三 赞了你的文章《Spring Boot 最佳实践》")
    private String content;

    @Schema(description = "关联目标ID", example = "10")
    private Long targetId;

    @Schema(description = "关联目标类型", example = "article")
    private String targetType;

    @Schema(description = "发送者ID", example = "2")
    private Long senderId;

    @Schema(description = "发送者昵称", example = "李四")
    private String senderNickname;

    @Schema(description = "发送者头像URL")
    private String senderAvatar;

    @Schema(description = "是否已读：0-未读 1-已读", example = "0")
    private Integer isRead;

    @Schema(description = "通知时间", example = "2024-03-21T15:30:00")
    private LocalDateTime createTime;

}
