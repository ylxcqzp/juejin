package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 评论用户ID */
    private Long userId;

    /** 目标ID（文章/沸点） */
    private Long targetId;

    /** 目标类型：1-文章 2-沸点 */
    private Integer targetType;

    /** 父评论ID（回复时指向被回复的评论） */
    private Long parentId;

    /** 根评论ID（一级评论为null，回复指向根评论） */
    private Long rootId;

    /** 被回复用户ID */
    private Long replyUserId;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 回复数 */
    private Integer replyCount;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 状态：0-已删除 1-正常 2-审核中 */
    private Integer status;

}
