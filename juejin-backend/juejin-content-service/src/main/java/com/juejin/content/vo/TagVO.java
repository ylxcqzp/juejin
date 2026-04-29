package com.juejin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签VO
 *
 * @author juejin
 */
@Data
@Schema(description = "标签信息")
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID", example = "1")
    private Long id;

    @Schema(description = "标签名称", example = "Java")
    private String name;

    @Schema(description = "标签图标URL", example = "/icons/java.png")
    private String icon;

}
