package com.juejin.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 *
 * @author juejin
 */
@Data
@Schema(description = "评论信息")
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评论ID", example = "30")
    private Long id;

    @Schema(description = "评论用户ID", example = "1")
    private Long userId;

    @Schema(description = "评论用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "评论用户头像URL")
    private String userAvatar;

    @Schema(description = "被评论的目标ID", example = "10")
    private Long targetId;

    @Schema(description = "目标类型：1-文章 2-沸点", example = "1")
    private Integer targetType;

    @Schema(description = "父评论ID（回复时）")
    private Long parentId;

    @Schema(description = "根评论ID（一级评论为null）")
    private Long rootId;

    @Schema(description = "被回复的用户ID")
    private Long replyUserId;

    @Schema(description = "被回复的用户昵称", example = "李四")
    private String replyUserNickname;

    @Schema(description = "评论内容", example = "写得很好，学到了很多！")
    private String content;

    @Schema(description = "点赞数", example = "5")
    private Integer likeCount;

    @Schema(description = "回复数", example = "3")
    private Integer replyCount;

    @Schema(description = "是否置顶：0-否 1-是", example = "0")
    private Integer isTop;

    @Schema(description = "状态：0-已删除 1-正常", example = "1")
    private Integer status;

    @Schema(description = "评论时间", example = "2024-03-20T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "子回复列表（最多嵌套2层）")
    private List<CommentVO> replies;

}
