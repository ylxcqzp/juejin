package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分类名称 */
    private String name;

    /** 分类描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 父分类ID */
    private Long parentId;

    /** 层级 */
    private Integer level;

    /** 文章数量 */
    private Integer articleCount;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

}
