package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String email;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private String backgroundImage;
    private Integer level;
    private Integer points;
    private Integer followingCount;
    private Integer followerCount;
    private Integer articleCount;
    private Integer likeCount;
    private Integer status;
    private Integer role;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginFailCount;
    private LocalDateTime lockUntilTime;
    private LocalDateTime cancelApplyTime;

}
