package com.juejin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "文章信息")
public class ArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文章ID", example = "1")
    private Long id;

    @Schema(description = "作者ID", example = "1")
    private Long authorId;

    @Schema(description = "作者昵称", example = "张三")
    private String authorNickname;

    @Schema(description = "作者头像URL")
    private String authorAvatar;

    @Schema(description = "文章标题", example = "Spring Boot 微服务最佳实践")
    private String title;

    @Schema(description = "文章摘要", example = "本文介绍了Spring Boot微服务架构的最佳实践...")
    private String summary;

    @Schema(description = "封面图URL", example = "https://cdn.example.com/article/cover1.png")
    private String coverImage;

    @Schema(description = "文章内容（Markdown格式）")
    private String content;

    @Schema(description = "文章内容（HTML格式，预留）")
    private String contentHtml;

    @Schema(description = "分类ID", example = "2")
    private Long categoryId;

    @Schema(description = "分类名称", example = "后端")
    private String categoryName;

    @Schema(description = "文章状态：0-草稿 1-审核中 2-已发布 3-已驳回", example = "2")
    private Integer status;

    @Schema(description = "发布时间", example = "2024-03-20T10:00:00")
    private LocalDateTime publishTime;

    @Schema(description = "浏览量", example = "1250")
    private Integer viewCount;

    @Schema(description = "点赞数", example = "42")
    private Integer likeCount;

    @Schema(description = "评论数", example = "15")
    private Integer commentCount;

    @Schema(description = "收藏数", example = "28")
    private Integer favoriteCount;

    @Schema(description = "是否置顶：0-否 1-是", example = "0")
    private Integer isTop;

    @Schema(description = "是否精华：0-否 1-是", example = "0")
    private Integer isEssence;

    @Schema(description = "是否原创：0-转载 1-原创", example = "1")
    private Integer isOriginal;

    @Schema(description = "文章标签列表")
    private List<TagVO> tags;

    @Schema(description = "创建时间", example = "2024-03-20T09:55:00")
    private LocalDateTime createTime;

}
