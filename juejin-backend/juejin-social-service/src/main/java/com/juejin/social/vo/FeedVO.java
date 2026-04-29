package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Feed流条目VO
 *
 * @author juejin
 */
@Data
@Schema(description = "Feed流内容条目")
public class FeedVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "内容类型：article-文章 pin-沸点", example = "article")
    private String feedType;

    @Schema(description = "内容ID", example = "10")
    private Long contentId;

    @Schema(description = "发布者ID", example = "1")
    private Long userId;

    @Schema(description = "发布者昵称", example = "张三")
    private String userNickname;

    @Schema(description = "发布者头像URL")
    private String userAvatar;

    @Schema(description = "标题（文章标题或沸点摘要）", example = "Spring Boot 微服务最佳实践")
    private String title;

    @Schema(description = "内容摘要")
    private String summary;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "标签列表（JSON格式）")
    private String tags;

    @Schema(description = "浏览量", example = "1250")
    private Integer viewCount;

    @Schema(description = "点赞数", example = "42")
    private Integer likeCount;

    @Schema(description = "评论数", example = "15")
    private Integer commentCount;

    @Schema(description = "发布时间", example = "2024-03-20T10:00:00")
    private LocalDateTime publishTime;

    @Schema(description = "内容来源：following-关注 hot-热门 recommend-推荐", example = "recommend")
    private String source;

}
