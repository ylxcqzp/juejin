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

    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 密码（加密存储） */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 个人简介 */
    private String bio;
    /** 背景图URL */
    private String backgroundImage;
    /** 等级 */
    private Integer level;
    /** 掘力值 */
    private Integer points;
    /** 关注数 */
    private Integer followingCount;
    /** 粉丝数 */
    private Integer followerCount;
    /** 文章数 */
    private Integer articleCount;
    /** 获赞数 */
    private Integer likeCount;
    /** 状态：0-正常 1-禁用 */
    private Integer status;
    /** 角色：0-普通用户 1-管理员 */
    private Integer role;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    /** 最后登录IP */
    private String lastLoginIp;
    /** 登录失败次数 */
    private Integer loginFailCount;
    /** 锁定截止时间 */
    private LocalDateTime lockUntilTime;
    /** 注销申请时间 */
    private LocalDateTime cancelApplyTime;

}
