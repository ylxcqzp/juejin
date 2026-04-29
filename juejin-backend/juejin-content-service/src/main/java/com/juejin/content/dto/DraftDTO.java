package com.juejin.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 草稿创建/更新请求DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "草稿创建/更新请求")
public class DraftDTO {

    @Size(max = 100, message = "标题最多100字")
    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章内容（Markdown）")
    private String content;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "标签ID列表（JSON格式）")
    private String tags;

    @Schema(description = "分类ID")
    private Long categoryId;

}
