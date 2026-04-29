package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏夹实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite_folder")
public class FavoriteFolder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 收藏夹名称 */
    private String name;

    /** 收藏夹描述 */
    private String description;

    /** 封面图 */
    private String coverImage;

    /** 收藏文章数 */
    private Integer articleCount;

    /** 是否默认收藏夹：0-否 1-是 */
    private Integer isDefault;

    /** 是否公开：0-私密 1-公开 */
    private Integer isPublic;

    /** 排序值 */
    private Integer sortOrder;

}
