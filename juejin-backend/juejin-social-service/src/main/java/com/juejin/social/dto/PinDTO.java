package com.juejin.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 沸点发布DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "沸点发布请求")
public class PinDTO {

    @NotBlank(message = "内容不能为空")
    @Size(max = 500, message = "沸点内容最多500字")
    @Schema(description = "沸点内容", required = true)
    private String content;

    @Schema(description = "图片URL列表（JSON数组）")
    private String images;

    @Schema(description = "链接地址")
    private String linkUrl;

    @Schema(description = "链接标题")
    private String linkTitle;

    @Schema(description = "链接封面")
    private String linkCover;

    @Schema(description = "话题ID")
    private Long topicId;

}
