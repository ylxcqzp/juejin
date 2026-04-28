package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "密码重置验证码请求")
public class PasswordResetDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "邮箱或手机号", example = "user@example.com")
    private String account;

    @NotBlank(message = "类型不能为空")
    @Schema(description = "账号类型：email/phone", example = "email")
    private String type;

}
