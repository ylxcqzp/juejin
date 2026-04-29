package com.juejin.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评论创建DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "评论创建请求")
public class CommentDTO {

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID（文章/沸点）", required = true)
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    @Schema(description = "目标类型：1-文章 2-沸点", required = true, example = "1")
    private Integer targetType;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容最多2000字")
    @Schema(description = "评论内容", required = true)
    private String content;

    @Schema(description = "父评论ID（回复时传）")
    private Long parentId;

    @Schema(description = "根评论ID（回复时传）")
    private Long rootId;

    @Schema(description = "被回复用户ID（回复时传）")
    private Long replyUserId;

}
