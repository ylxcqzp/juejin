package com.juejin.content.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.DraftDTO;
import com.juejin.content.service.DraftService;
import com.juejin.content.vo.DraftVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 草稿控制器
 *
 * @author juejin
 */
@Tag(name = "草稿管理", description = "文章草稿的保存、编辑、删除、发布等相关接口")
@RestController
@RequestMapping("/api/v1/drafts")
@RequiredArgsConstructor
@Validated
public class DraftController {

    private final DraftService draftService;

    @Operation(summary = "获取草稿列表", description = "分页获取当前用户的草稿列表")
    @GetMapping
    public Result<PageResult<DraftVO>> getDraftList(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(draftService.getDraftList(userId, page, size));
    }

    @Operation(summary = "获取草稿详情", description = "获取草稿的详细内容")
    @GetMapping("/{draftId}")
    public Result<DraftVO> getDraftById(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "草稿ID", required = true) @PathVariable Long draftId) {
        return Result.success(draftService.getDraftById(userId, draftId));
    }

    @Operation(summary = "保存草稿", description = "新建草稿")
    @PostMapping
    public Result<DraftVO> saveDraft(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated DraftDTO dto) {
        return Result.success(draftService.saveDraft(userId, dto));
    }

    @Operation(summary = "自动保存草稿", description = "更新已有草稿或创建新草稿，用于定时自动保存")
    @PutMapping("/{draftId}/auto-save")
    public Result<DraftVO> autoSave(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "草稿ID（可选，不传则新建）") @PathVariable(required = false) Long draftId,
            @RequestBody @Validated DraftDTO dto) {
        return Result.success(draftService.autoSave(userId, draftId, dto));
    }

    @Operation(summary = "删除草稿", description = "删除指定草稿")
    @DeleteMapping("/{draftId}")
    public Result<Boolean> deleteDraft(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "草稿ID", required = true) @PathVariable Long draftId) {
        return Result.success(draftService.deleteDraft(userId, draftId));
    }

    @Operation(summary = "从草稿发布", description = "将草稿发布为正式文章，发布后草稿自动删除")
    @PostMapping("/{draftId}/publish")
    public Result<Long> publishFromDraft(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "草稿ID", required = true) @PathVariable Long draftId,
            @RequestBody @Validated DraftDTO dto) {
        return Result.success(draftService.publishFromDraft(userId, draftId, dto));
    }

}
