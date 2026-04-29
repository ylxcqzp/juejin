package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注用户列表项VO
 *
 * @author juejin
 */
@Data
@Schema(description = "关注/粉丝列表用户信息")
public class FollowUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "2")
    private Long id;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "用户头像URL", example = "https://cdn.example.com/avatar/2.png")
    private String avatar;

    @Schema(description = "用户简介", example = "后端开发工程师")
    private String bio;

    @Schema(description = "用户等级：1-6", example = "3")
    private Integer level;

    @Schema(description = "是否互相关注", example = "true")
    private Boolean isMutualFollow;

    @Schema(description = "关注时间", example = "2024-03-10T14:20:00")
    private LocalDateTime followTime;

}
