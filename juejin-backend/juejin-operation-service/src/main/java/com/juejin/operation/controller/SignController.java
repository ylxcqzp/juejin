package com.juejin.operation.controller;

import com.juejin.common.result.Result;
import com.juejin.operation.service.SignService;
import com.juejin.operation.vo.SignVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 签到控制器
 *
 * @author juejin
 */
@Tag(name = "签到管理", description = "每日签到、签到状态查询相关接口")
@RestController
@RequestMapping("/api/v1/signs")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @Operation(summary = "每日签到", description = "执行每日签到，获得掘力值奖励")
    @PostMapping
    public Result<SignVO> signIn(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(signService.signIn(userId));
    }

    @Operation(summary = "查询签到状态", description = "查询今日签到状态和当月签到日历")
    @GetMapping("/status")
    public Result<SignVO> getSignStatus(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(signService.getSignStatus(userId));
    }

}
