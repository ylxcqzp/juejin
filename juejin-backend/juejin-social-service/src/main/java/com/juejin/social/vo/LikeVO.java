package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞记录VO
 *
 * @author juejin
 */
@Data
@Schema(description = "点赞信息")
public class LikeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "点赞记录ID", example = "100")
    private Long id;

    @Schema(description = "点赞用户ID", example = "1")
    private Long userId;

    @Schema(description = "点赞用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "点赞用户头像URL")
    private String userAvatar;

    @Schema(description = "被点赞的目标ID", example = "10")
    private Long targetId;

    @Schema(description = "目标类型：1-文章 2-评论 3-沸点", example = "1")
    private Integer targetType;

    @Schema(description = "点赞时间", example = "2024-03-20T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "当前用户是否已点赞", example = "true")
    private Boolean isLiked;

    @Schema(description = "该目标的点赞总数", example = "42")
    private Long likeCount;

}
