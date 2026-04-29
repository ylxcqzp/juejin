package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 沸点VO
 *
 * @author juejin
 */
@Data
@Schema(description = "沸点动态信息")
public class PinVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "沸点ID", example = "20")
    private Long id;

    @Schema(description = "发布者用户ID", example = "1")
    private Long userId;

    @Schema(description = "发布者昵称", example = "张三")
    private String userNickname;

    @Schema(description = "发布者头像URL")
    private String userAvatar;

    @Schema(description = "沸点内容（最多500字）", example = "今天刚学到一个很实用的技巧...")
    private String content;

    @Schema(description = "图片列表（JSON数组）", example = "[\"https://cdn.example.com/img1.png\"]")
    private String images;

    @Schema(description = "分享链接地址")
    private String linkUrl;

    @Schema(description = "链接标题")
    private String linkTitle;

    @Schema(description = "链接封面图")
    private String linkCover;

    @Schema(description = "所属话题ID")
    private Long topicId;

    @Schema(description = "话题名称", example = "前端开发")
    private String topicName;

    @Schema(description = "点赞数", example = "15")
    private Integer likeCount;

    @Schema(description = "评论数", example = "8")
    private Integer commentCount;

    @Schema(description = "分享数", example = "3")
    private Integer shareCount;

    @Schema(description = "是否热门：0-否 1-是", example = "0")
    private Integer isHot;

    @Schema(description = "当前用户是否已点赞", example = "false")
    private Boolean isLiked;

    @Schema(description = "发布时间", example = "2024-03-21T09:00:00")
    private LocalDateTime createTime;

}
