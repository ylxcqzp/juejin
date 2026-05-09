package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_follow")
public class UserFollow extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关注者用户ID */
    private Long userId;
    /** 被关注用户ID */
    private Long followingId;
    /** 状态：0-取消关注 1-已关注 */
    private Integer status;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

}
