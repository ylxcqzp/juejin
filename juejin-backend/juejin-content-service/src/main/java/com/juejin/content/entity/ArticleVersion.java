package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章历史版本实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_version")
public class ArticleVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文章ID */
    private Long articleId;

    /** 版本号 */
    private Integer version;

    /** 标题 */
    private String title;

    /** 内容（Markdown） */
    private String content;

    /** 内容（HTML） */
    private String contentHtml;

    /** 变更摘要 */
    private String changeSummary;

}
