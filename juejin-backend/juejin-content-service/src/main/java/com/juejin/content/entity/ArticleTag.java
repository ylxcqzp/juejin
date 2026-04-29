package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章标签关联实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_tag")
public class ArticleTag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文章ID */
    private Long articleId;

    /** 标签ID */
    private Long tagId;

}
