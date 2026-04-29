package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 沸点实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pin")
public class Pin extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 内容（最多500字） */
    private String content;

    /** 图片列表（JSON数组） */
    private String images;

    /** 链接地址 */
    private String linkUrl;

    /** 链接标题 */
    private String linkTitle;

    /** 链接封面 */
    private String linkCover;

    /** 话题ID */
    private Long topicId;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 分享数 */
    private Integer shareCount;

    /** 是否热门 */
    private Integer isHot;

    /** 状态：0-删除 1-正常 2-审核中 */
    private Integer status;

}
