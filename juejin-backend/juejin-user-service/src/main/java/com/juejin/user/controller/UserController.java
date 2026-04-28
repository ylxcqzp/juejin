package com.juejin.user.controller;

import com.juejin.common.result.Result;
import com.juejin.user.dto.LoginDTO;
import com.juejin.user.dto.RegisterDTO;
import com.juejin.user.service.UserService;
import com.juejin.user.vo.LoginVO;
import com.juejin.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Validated RegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success(userVO);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

}
