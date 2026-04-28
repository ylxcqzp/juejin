package com.juejin.content.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章VO
 *
 * @author juejin
 */
@Data
public class ArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文章ID */
    private Long id;

    /** 作者ID */
    private Long authorId;

    /** 作者昵称 */
    private String authorNickname;

    /** 作者头像 */
    private String authorAvatar;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 封面图 */
    private String coverImage;

    /** 内容 */
    private String content;

    /** 内容（HTML） */
    private String contentHtml;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 状态 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 是否置顶 */
    private Integer isTop;

    /** 是否精华 */
    private Integer isEssence;

    /** 是否原创 */
    private Integer isOriginal;

    /** 标签列表 */
    private List<TagVO> tags;

    /** 创建时间 */
    private LocalDateTime createTime;

}
