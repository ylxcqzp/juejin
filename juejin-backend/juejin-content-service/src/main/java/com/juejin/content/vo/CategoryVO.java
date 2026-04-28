package com.juejin.content.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类VO
 *
 * @author juejin
 */
@Data
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 分类描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 文章数量 */
    private Integer articleCount;

}
