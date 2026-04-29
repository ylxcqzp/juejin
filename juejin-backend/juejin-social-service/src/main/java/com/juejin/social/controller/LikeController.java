package com.juejin.social.controller;

import com.juejin.common.result.Result;
import com.juejin.social.dto.LikeDTO;
import com.juejin.social.service.LikeService;
import com.juejin.social.vo.LikeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点赞控制器
 *
 * @author juejin
 */
@Tag(name = "点赞管理", description = "点赞、取消点赞、点赞列表相关接口")
@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
@Validated
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "点赞/取消点赞", description = "对目标进行点赞或取消点赞，已点赞则取消，未点赞则点赞")
    @PostMapping
    public Result<LikeVO> toggleLike(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated LikeDTO dto) {
        return Result.success(likeService.toggleLike(userId, dto));
    }

    @Operation(summary = "获取点赞列表", description = "获取目标的点赞用户列表")
    @GetMapping
    public Result<List<LikeVO>> getLikeList(
            @Parameter(description = "目标ID", required = true) @RequestParam Long targetId,
            @Parameter(description = "目标类型：1-文章 2-评论 3-沸点", required = true) @RequestParam Integer targetType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(likeService.getLikeList(targetId, targetType, page, size));
    }

    @Operation(summary = "检查点赞状态", description = "检查当前用户是否已点赞目标")
    @GetMapping("/status")
    public Result<Boolean> checkLikeStatus(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "目标ID", required = true) @RequestParam Long targetId,
            @Parameter(description = "目标类型", required = true) @RequestParam Integer targetType) {
        return Result.success(likeService.isLiked(userId, targetId, targetType));
    }

}
