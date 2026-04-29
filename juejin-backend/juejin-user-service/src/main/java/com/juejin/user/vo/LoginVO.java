package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author juejin
 */
@Data
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "JWT访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "JWT刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "令牌过期时间（秒）", example = "1800")
    private Long expiresIn;

    @Schema(description = "用户基本信息")
    private UserVO userInfo;

}
