package com.juejin.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 标签创建/更新请求DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "标签创建/更新请求")
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称最多50字")
    @Schema(description = "标签名称", example = "Java")
    private String name;

    @Size(max = 200, message = "标签描述最多200字")
    @Schema(description = "标签描述", example = "Java编程语言相关技术")
    private String description;

    @Schema(description = "标签图标URL")
    private String icon;

    @Schema(description = "排序值，越小越靠前", example = "0")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

}
