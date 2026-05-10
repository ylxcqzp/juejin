package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息 VO
 *
 * @author juejin
 */
@Data
@Schema(description = "消息视图")
public class MessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者昵称")
    private String senderNickname;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "内容类型：1-文本 2-图片")
    private Integer contentType;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "是否已撤回")
    private Boolean isRecalled;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}
