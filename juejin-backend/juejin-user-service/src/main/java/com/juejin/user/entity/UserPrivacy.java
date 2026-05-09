package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_privacy")
public class UserPrivacy extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
    /** 是否公开收藏夹：0-否 1-是 */
    private Integer showFavorites;
    /** 是否公开关注列表：0-否 1-是 */
    private Integer showFollowing;
    /** 是否公开粉丝列表：0-否 1-是 */
    private Integer showFollowers;
    /** 是否允许陌生人私信：0-否 1-是 */
    private Integer allowStrangerMessage;
    /** 是否开启消息推送：0-否 1-是 */
    private Integer messagePushEnabled;
    /** 免打扰开始时间 */
    private LocalTime doNotDisturbStart;
    /** 免打扰结束时间 */
    private LocalTime doNotDisturbEnd;

}
