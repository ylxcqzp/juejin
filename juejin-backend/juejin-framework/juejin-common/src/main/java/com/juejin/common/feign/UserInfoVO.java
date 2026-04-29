package com.juejin.common.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO（Feign调用用）
 *
 * @author juejin
 */
@Data
@Schema(description = "用户信息（Feign调用返回）")
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像URL", example = "https://cdn.example.com/avatar/1.png")
    private String avatar;

    @Schema(description = "用户等级：1-6", example = "3")
    private Integer level;

    @Schema(description = "角色：0-普通用户 1-认证作者 2-管理员", example = "0")
    private Integer role;

    @Schema(description = "注册时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createTime;

}
