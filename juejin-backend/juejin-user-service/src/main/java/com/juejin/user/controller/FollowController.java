package com.juejin.user.controller;

import com.juejin.common.dto.PageParam;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.user.service.FollowService;
import com.juejin.user.vo.FollowStatusVO;
import com.juejin.user.vo.FollowUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "关注管理", description = "关注/取消关注、关注列表、粉丝列表等相关接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/{id}/follow")
    public Result<Void> follow(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "目标用户ID", required = true) @PathVariable Long id) {
        followService.follow(userId, id);
        return Result.success();
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/{id}/follow")
    public Result<Void> unfollow(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "目标用户ID", required = true) @PathVariable Long id) {
        followService.unfollow(userId, id);
        return Result.success();
    }

    @Operation(summary = "关注列表", description = "获取指定用户的关注列表，分页返回")
    @GetMapping("/{id}/following")
    public Result<PageResult<FollowUserVO>> getFollowingList(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Validated PageParam pageParam) {
        return Result.success(followService.getFollowingList(id, pageParam));
    }

    @Operation(summary = "粉丝列表", description = "获取指定用户的粉丝列表，分页返回")
    @GetMapping("/{id}/followers")
    public Result<PageResult<FollowUserVO>> getFollowerList(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Validated PageParam pageParam) {
        return Result.success(followService.getFollowerList(id, pageParam));
    }

    @Operation(summary = "移除粉丝", description = "移除指定粉丝（踢出粉丝列表）")
    @DeleteMapping("/followers/{id}")
    public Result<Void> removeFollower(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "粉丝用户ID", required = true) @PathVariable Long id) {
        followService.removeFollower(userId, id);
        return Result.success();
    }

    @Operation(summary = "检查关注状态", description = "检查当前用户与目标用户的关注状态（是否关注、是否被关注、是否互关）")
    @GetMapping("/{id}/follow/status")
    public Result<FollowStatusVO> checkFollowStatus(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "目标用户ID", required = true) @PathVariable Long id) {
        return Result.success(followService.checkFollowStatus(userId, id));
    }
}
