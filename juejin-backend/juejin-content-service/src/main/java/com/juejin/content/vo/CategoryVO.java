package com.juejin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类VO
 *
 * @author juejin
 */
@Data
@Schema(description = "文章分类信息")
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID", example = "2")
    private Long id;

    @Schema(description = "分类名称", example = "后端")
    private String name;

    @Schema(description = "分类描述", example = "后端开发相关技术")
    private String description;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "该分类下的文章数量", example = "128")
    private Integer articleCount;

}
