package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 草稿实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("draft")
public class Draft extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容（Markdown） */
    private String content;

    /** 封面图 */
    private String coverImage;

    /** 标签JSON数组 */
    private String tags;

    /** 分类ID */
    private Long categoryId;

    /** 关联文章ID（编辑已有文章时） */
    private Long articleId;

    /** 最后自动保存时间 */
    private LocalDateTime autoSaveTime;

}
