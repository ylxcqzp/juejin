package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_oauth")
public class UserOauth extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
    /** 第三方平台：github/wechat/qq */
    private String platform;
    /** 第三方平台用户ID */
    private String oauthId;
    /** 第三方平台用户名 */
    private String oauthName;
    /** 第三方平台头像 */
    private String oauthAvatar;
    /** 绑定时间 */
    private LocalDateTime bindTime;

}
