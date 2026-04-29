package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点赞记录实体类（高并发表，使用乐观锁）
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("like_record")
public class LikeRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 目标ID（文章/评论/沸点） */
    private Long targetId;

    /** 目标类型：1-文章 2-评论 3-沸点 */
    private Integer targetType;

    /** 状态：0-取消 1-已点赞 */
    private Integer status;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

}
