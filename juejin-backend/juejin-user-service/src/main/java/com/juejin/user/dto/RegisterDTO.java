package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 邮箱注册请求DTO
 *
 * @author juejin
 */
@Data
@Schema(description = "邮箱注册请求")
public class RegisterDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "邮箱地址", required = true, example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    @Schema(description = "密码（6-20位）", required = true, example = "a123456")
    private String password;

    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")
    @Schema(description = "昵称（2-20字）", required = true, example = "张三")
    private String nickname;

}
