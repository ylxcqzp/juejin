package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
public class Tag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 标签名称 */
    private String name;

    /** 标签描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 父标签ID */
    private Long parentId;

    /** 层级 */
    private Integer level;

    /** 文章数量 */
    private Integer articleCount;

    /** 关注数量 */
    private Integer followCount;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

}
