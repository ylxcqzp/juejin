package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文章实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 作者ID */
    private Long authorId;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 封面图 */
    private String coverImage;

    /** 内容（Markdown） */
    private String content;

    /** 内容（HTML） */
    private String contentHtml;

    /** 分类ID */
    private Long categoryId;

    /** 状态：0-草稿 1-审核中 2-已发布 3-已拒绝 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 定时发布时间 */
    private LocalDateTime scheduledTime;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 分享数 */
    private Integer shareCount;

    /** 是否置顶 */
    private Integer isTop;

    /** 是否精华 */
    private Integer isEssence;

    /** 是否原创 */
    private Integer isOriginal;

    /** 原文链接 */
    private String sourceUrl;

    /** 版权设置 */
    private Integer copyright;

    /** 版本号 */
    private Integer version;

}
