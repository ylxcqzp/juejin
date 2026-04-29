package com.juejin.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 分类创建/更新请求DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "分类创建/更新请求")
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最多50字")
    @Schema(description = "分类名称", example = "后端")
    private String name;

    @Size(max = 200, message = "分类描述最多200字")
    @Schema(description = "分类描述", example = "后端开发相关技术")
    private String description;

    @Schema(description = "分类图标URL")
    private String icon;

    @Schema(description = "排序值，越小越靠前", example = "0")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

}
