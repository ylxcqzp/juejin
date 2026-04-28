package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "绑定账号请求")
public class BindAccountDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "邮箱或手机号", example = "user@example.com")
    private String account;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "888888")
    private String code;

}
