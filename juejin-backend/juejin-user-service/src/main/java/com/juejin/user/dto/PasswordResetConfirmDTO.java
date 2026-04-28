package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "确认重置密码请求")
public class PasswordResetConfirmDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "邮箱或手机号", example = "user@example.com")
    private String account;

    @NotBlank(message = "类型不能为空")
    @Schema(description = "账号类型：email/phone", example = "email")
    private String type;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "888888")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    @Schema(description = "新密码", example = "newPassword123")
    private String newPassword;

}
