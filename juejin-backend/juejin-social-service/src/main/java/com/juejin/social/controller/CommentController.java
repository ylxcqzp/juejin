package com.juejin.social.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.CommentDTO;
import com.juejin.social.service.CommentService;
import com.juejin.social.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 *
 * @author juejin
 */
@Tag(name = "评论管理", description = "评论发表、回复、删除、置顶等相关接口")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评论", description = "发表评论或回复评论")
    @PostMapping
    public Result<CommentVO> createComment(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated CommentDTO dto) {
        return Result.success(commentService.createComment(userId, dto));
    }

    @Operation(summary = "删除评论", description = "删除评论，仅评论发布者可操作")
    @DeleteMapping("/{commentId}")
    public Result<Boolean> deleteComment(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        return Result.success(commentService.deleteComment(userId, commentId));
    }

    @Operation(summary = "置顶/取消置顶", description = "置顶或取消置顶评论，需作者权限")
    @PutMapping("/{commentId}/top")
    public Result<Boolean> topComment(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        return Result.success(commentService.topComment(userId, commentId));
    }

    @Operation(summary = "获取评论列表", description = "分页获取目标的评论列表，支持嵌套回复")
    @GetMapping
    public Result<PageResult<CommentVO>> getCommentList(
            @Parameter(description = "目标ID（文章/沸点）", required = true) @RequestParam Long targetId,
            @Parameter(description = "目标类型：1-文章 2-沸点", required = true) @RequestParam Integer targetType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序方式：time-最新 hot-最热") @RequestParam(defaultValue = "time") String sortBy) {
        return Result.success(commentService.getCommentList(targetId, targetType, page, size, sortBy));
    }

}
