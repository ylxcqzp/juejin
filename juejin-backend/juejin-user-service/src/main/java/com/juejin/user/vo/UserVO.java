package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户基本信息VO
 *
 * @author juejin
 */
@Data
@Schema(description = "用户基本信息")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://cdn.example.com/avatar/default.png")
    private String avatar;

    @Schema(description = "个人简介", example = "一名热爱技术的全栈开发者")
    private String bio;

    @Schema(description = "个人主页背景图URL")
    private String backgroundImage;

    @Schema(description = "用户等级：1-萌新 2-初学者 3-进阶者 4-熟练者 5-专家 6-大神", example = "3")
    private Integer level;

    @Schema(description = "掘力值", example = "520")
    private Integer points;

    @Schema(description = "关注数", example = "42")
    private Integer followingCount;

    @Schema(description = "粉丝数", example = "128")
    private Integer followerCount;

    @Schema(description = "文章数", example = "15")
    private Integer articleCount;

    @Schema(description = "获赞数", example = "256")
    private Integer likeCount;

    @Schema(description = "角色：0-普通用户 1-认证作者 2-管理员", example = "0")
    private Integer role;

    @Schema(description = "注册时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createTime;

}
