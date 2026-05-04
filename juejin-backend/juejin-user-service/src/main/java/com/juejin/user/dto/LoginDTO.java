package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @Schema(description = "邮箱（loginType=email时必填）", example = "user@example.com")
    private String email;

    @Schema(description = "手机号（loginType=phone时必填）", example = "13800138000")
    private String phone;

    @Schema(description = "登录方式：email/phone，默认email", example = "email")
    private String loginType = "email";

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

}
