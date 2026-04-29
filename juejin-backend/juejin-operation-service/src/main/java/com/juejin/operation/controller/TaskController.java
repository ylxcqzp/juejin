package com.juejin.operation.controller;

import com.juejin.common.result.Result;
import com.juejin.operation.service.TaskService;
import com.juejin.operation.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 *
 * @author juejin
 */
@Tag(name = "任务管理", description = "任务查询、进度追踪、奖励领取相关接口")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "获取所有任务", description = "获取用户所有任务及进度")
    @GetMapping
    public Result<List<TaskVO>> getUserTasks(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(taskService.getUserTasks(userId));
    }

    @Operation(summary = "按类型获取任务", description = "获取指定类型的任务列表")
    @GetMapping("/type/{type}")
    public Result<List<TaskVO>> getTasksByType(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "任务类型：newbie/daily", required = true) @PathVariable String type) {
        return Result.success(taskService.getTasksByType(userId, type));
    }

    @Operation(summary = "领取任务奖励", description = "完成任务后领取掘力值奖励")
    @PostMapping("/{taskId}/claim")
    public Result<TaskVO> claimReward(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        return Result.success(taskService.claimReward(userId, taskId));
    }

}
