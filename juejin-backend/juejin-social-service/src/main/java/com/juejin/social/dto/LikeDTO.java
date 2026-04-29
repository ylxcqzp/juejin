package com.juejin.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 点赞请求DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "点赞请求")
public class LikeDTO {

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID（文章/评论/沸点）", required = true)
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    @Schema(description = "目标类型：1-文章 2-评论 3-沸点", required = true, example = "1")
    private Integer targetType;

}
