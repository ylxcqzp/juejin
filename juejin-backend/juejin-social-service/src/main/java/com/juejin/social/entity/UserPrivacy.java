package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 用户隐私设置实体（social-service 本地映射，用于跨表查询）
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_privacy")
public class UserPrivacy extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Integer showFavorites;
    private Integer showFollowing;
    private Integer showFollowers;
    private Integer allowStrangerMessage;
    private Integer messagePushEnabled;
    private LocalTime doNotDisturbStart;
    private LocalTime doNotDisturbEnd;
}
