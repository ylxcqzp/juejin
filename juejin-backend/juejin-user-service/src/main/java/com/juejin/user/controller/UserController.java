package com.juejin.user.controller;

import com.juejin.common.result.Result;
import com.juejin.user.dto.*;
import com.juejin.user.service.UserService;
import com.juejin.user.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户注册、登录、信息查询等相关接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "邮箱注册", description = "新用户邮箱注册接口")
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Validated RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @Operation(summary = "手机号注册", description = "新用户手机号注册接口，需先获取短信验证码")
    @PostMapping("/register/phone")
    public Result<UserVO> registerByPhone(@RequestBody @Validated PhoneRegisterDTO dto) {
        return Result.success(userService.registerByPhone(dto));
    }

    @Operation(summary = "用户登录", description = "用户登录接口，支持邮箱和手机号，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "发送验证码", description = "发送邮箱或手机验证码")
    @PostMapping("/verify-code")
    public Result<Void> sendVerifyCode(@RequestBody @Validated VerifyCodeDTO dto) {
        userService.sendVerifyCode(dto);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户公开信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "获取用户公开资料", description = "获取用户完整公开资料（含社交链接）")
    @GetMapping("/{id}/profile")
    public Result<UserProfileVO> getPublicProfile(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        return Result.success(userService.getPublicProfile(id));
    }

    @Operation(summary = "获取我的个人资料", description = "获取当前登录用户的完整个人资料（含隐私设置、徽章等）")
    @GetMapping("/profile")
    public Result<UserProfileVO> getMyProfile(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @Operation(summary = "更新个人资料", description = "更新当前用户的昵称、头像、简介等")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated UpdateProfileDTO dto) {
        userService.updateProfile(userId, dto);
        return Result.success();
    }

    @Operation(summary = "修改密码", description = "修改当前用户的登录密码，需要原密码验证")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated ChangePasswordDTO dto) {
        userService.changePassword(userId, dto);
        return Result.success();
    }

    @Operation(summary = "更新技能标签", description = "更新用户技能标签，最多5个")
    @PutMapping("/tags")
    public Result<Void> updateUserTags(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody List<Long> tagIds) {
        userService.updateUserTags(userId, tagIds);
        return Result.success();
    }

    @Operation(summary = "更新社交链接", description = "更新用户的社交链接（GitHub、博客等）")
    @PutMapping("/social-links")
    public Result<Void> updateSocialLinks(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated UpdateSocialLinksDTO dto) {
        userService.updateSocialLinks(userId, dto.getLinks());
        return Result.success();
    }

    @Operation(summary = "查询掘力值与等级", description = "获取当前用户的掘力值和等级信息")
    @GetMapping("/points")
    public Result<PointsVO> getPoints(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getPoints(userId));
    }

    @Operation(summary = "获取用户徽章", description = "获取当前用户获得的徽章列表")
    @GetMapping("/badges")
    public Result<List<UserBadgeVO>> getMyBadges(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getUserBadges(userId));
    }

    @Operation(summary = "绑定手机号", description = "绑定手机号到当前账号")
    @PostMapping("/bind/phone")
    public Result<Void> bindPhone(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated BindAccountDTO dto) {
        userService.bindPhone(userId, dto);
        return Result.success();
    }

    @Operation(summary = "绑定邮箱", description = "绑定邮箱到当前账号")
    @PostMapping("/bind/email")
    public Result<Void> bindEmail(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated BindAccountDTO dto) {
        userService.bindEmail(userId, dto);
        return Result.success();
    }

    @Operation(summary = "解绑账号", description = "解绑手机号或邮箱")
    @DeleteMapping("/unbind/{type}")
    public Result<Void> unbindAccount(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "解绑类型：phone/email", required = true) @PathVariable String type) {
        userService.unbindAccount(userId, type);
        return Result.success();
    }

    @Operation(summary = "申请注销账号", description = "提交账号注销申请，进入30天冷静期")
    @PostMapping("/account/cancel")
    public Result<Void> applyAccountCancellation(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        userService.applyAccountCancellation(userId);
        return Result.success();
    }

    @Operation(summary = "撤销注销申请", description = "在30天冷静期内撤销账号注销申请")
    @PostMapping("/account/cancel/revoke")
    public Result<Void> revokeAccountCancellation(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        userService.revokeAccountCancellation(userId);
        return Result.success();
    }
}
