package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话 VO
 *
 * @author juejin
 */
@Data
@Schema(description = "会话视图")
public class ConversationVO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "对方用户ID")
    private Long otherUserId;

    @Schema(description = "对方用户昵称")
    private String otherUserNickname;

    @Schema(description = "对方用户头像")
    private String otherUserAvatar;

    @Schema(description = "最后一条消息")
    private String lastMessage;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "当前用户未读数")
    private Integer unreadCount;
}
