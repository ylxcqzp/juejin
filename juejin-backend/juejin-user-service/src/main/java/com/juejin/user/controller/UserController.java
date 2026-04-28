package com.juejin.user.controller;

import com.juejin.common.result.Result;
import com.juejin.user.dto.LoginDTO;
import com.juejin.user.dto.RegisterDTO;
import com.juejin.user.service.UserService;
import com.juejin.user.vo.LoginVO;
import com.juejin.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author juejin
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息查询等相关接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 用户信息
     */
    @Operation(summary = "用户注册", description = "新用户注册接口")
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Validated RegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success(userVO);
    }

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 登录结果（包含Token）
     */
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID（从请求头获取）
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户公开信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

}
