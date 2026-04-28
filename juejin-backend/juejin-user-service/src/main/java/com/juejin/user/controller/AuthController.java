package com.juejin.user.controller;

import com.juejin.common.result.Result;
import com.juejin.user.dto.PasswordResetConfirmDTO;
import com.juejin.user.dto.PasswordResetDTO;
import com.juejin.user.service.UserService;
import com.juejin.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "Token刷新、密码重置等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;

    @Operation(summary = "刷新Token", description = "使用refreshToken刷新Access Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(
            @Parameter(description = "Refresh Token", required = true)
            @RequestParam("refreshToken") String refreshToken) {
        return Result.success(userService.refreshToken(refreshToken));
    }

    @Operation(summary = "发送密码重置验证码", description = "向邮箱或手机号发送密码重置验证码")
    @PostMapping("/password/reset")
    public Result<Void> sendPasswordResetCode(@RequestBody @Validated PasswordResetDTO dto) {
        userService.sendPasswordResetCode(dto);
        return Result.success();
    }

    @Operation(summary = "确认重置密码", description = "使用验证码重置密码")
    @PostMapping("/password/reset/confirm")
    public Result<Void> resetPassword(@RequestBody @Validated PasswordResetConfirmDTO dto) {
        userService.resetPassword(dto);
        return Result.success();
    }
}
