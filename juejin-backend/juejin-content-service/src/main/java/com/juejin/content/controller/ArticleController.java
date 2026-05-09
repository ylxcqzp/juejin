package com.juejin.content.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.ArticleCreateDTO;
import com.juejin.content.dto.ArticleUpdateDTO;
import com.juejin.content.service.ArticleService;
import com.juejin.content.vo.ArticleVO;
import com.juejin.content.vo.ArticleVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 *
 * @author juejin
 */
@Tag(name = "文章管理", description = "文章的发布、编辑、查询等相关接口")
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleService articleService;

    // ==================== 草稿管理（合并到 article.status=0） ====================

    @Operation(summary = "获取草稿列表", description = "分页获取当前用户的草稿列表（article.status=0）")
    @GetMapping("/drafts")
    public Result<PageResult<ArticleVO>> getDraftList(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(articleService.getUserDrafts(userId, page, size));
    }

    @Operation(summary = "获取草稿详情", description = "获取草稿的详细内容（仅作者可查看）")
    @GetMapping("/drafts/{draftId}")
    public Result<ArticleVO> getDraftById(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "草稿ID") @PathVariable Long draftId) {
        return Result.success(articleService.getDraftById(userId, draftId));
    }

    @Operation(summary = "创建草稿", description = "新建草稿（article.status=0），标题/内容等均为可选")
    @PostMapping("/drafts")
    public Result<ArticleVO> createDraft(
            @RequestBody ArticleCreateDTO dto,
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleService.createDraft(dto, userId));
    }

    @Operation(summary = "更新草稿", description = "更新已有草稿（仅 status=0 的文章可操作）")
    @PutMapping("/drafts/{draftId}")
    public Result<ArticleVO> updateDraft(
            @Parameter(description = "草稿ID") @PathVariable Long draftId,
            @RequestBody ArticleUpdateDTO dto,
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleService.updateDraft(draftId, dto, userId));
    }

    @Operation(summary = "删除草稿", description = "删除指定草稿（逻辑删除）")
    @DeleteMapping("/drafts/{draftId}")
    public Result<Boolean> deleteDraft(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "草稿ID") @PathVariable Long draftId) {
        return Result.success(articleService.deleteDraft(draftId, userId));
    }

    /**
     * 创建文章
     *
     * @param dto    文章信息
     * @param userId 作者ID
     * @return 创建的文章
     */
    @Operation(summary = "创建文章", description = "发布新文章")
    @PostMapping
    public Result<ArticleVO> createArticle(
            @RequestBody @Validated ArticleCreateDTO dto,
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        ArticleVO articleVO = articleService.createArticle(dto, userId);
        return Result.success(articleVO);
    }

    /**
     * 更新文章
     *
     * @param id     文章ID
     * @param dto    文章信息
     * @param userId 作者ID
     * @return 更新后的文章
     */
    @Operation(summary = "更新文章", description = "更新已有文章，需要作者权限")
    @PutMapping("/{id}")
    public Result<ArticleVO> updateArticle(
            @Parameter(description = "文章ID", required = true)
            @PathVariable Long id,
            @RequestBody @Validated ArticleUpdateDTO dto,
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        dto.setId(id);
        ArticleVO articleVO = articleService.updateArticle(dto, userId);
        return Result.success(articleVO);
    }

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @Operation(summary = "获取文章详情", description = "根据文章ID获取详细信息，同时增加浏览量")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleById(
            @Parameter(description = "文章ID", required = true)
            @PathVariable Long id) {
        ArticleVO articleVO = articleService.getArticleById(id);
        return Result.success(articleVO);
    }

    /**
     * 删除文章
     *
     * @param id     文章ID
     * @param userId 作者ID
     * @return 是否成功
     */
    @Operation(summary = "删除文章", description = "删除文章，需要作者权限")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteArticle(
            @Parameter(description = "文章ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        boolean result = articleService.deleteArticle(id, userId);
        return Result.success(result);
    }

    /**
     * 获取文章列表
     *
     * @param page       页码
     * @param size       每页数量
     * @param categoryId 分类ID（可选）
     * @return 文章列表
     */
    @Operation(summary = "获取文章列表", description = "分页获取文章列表，支持按分类筛选")
    @GetMapping
    public Result<PageResult<ArticleVO>> getArticleList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类ID")
            @RequestParam(required = false) Long categoryId) {
        PageResult<ArticleVO> result = articleService.getArticleList(page, size, categoryId);
        return Result.success(result);
    }

    /**
     * 获取用户文章列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 文章列表
     */
    @Operation(summary = "获取用户文章列表", description = "获取指定用户发布的文章列表")
    @GetMapping("/user/{userId}")
    public Result<PageResult<ArticleVO>> getUserArticles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getUserArticles(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取热门文章
     *
     * @param limit 数量限制
     * @return 热门文章列表
     */
    @Operation(summary = "获取热门文章", description = "根据浏览量获取热门文章")
    @GetMapping("/hot")
    public Result<List<ArticleVO>> getHotArticles(
            @Parameter(description = "数量限制", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ArticleVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    // ==================== 审核管理 ====================

    @Operation(summary = "提交审核", description = "提交文章进入审核流程，仅草稿和已驳回状态可操作")
    @PostMapping("/{id}/review")
    public Result<Boolean> submitForReview(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleService.submitForReview(id, userId));
    }

    @Operation(summary = "审核通过", description = "审核通过文章（管理员操作）")
    @PostMapping("/{id}/approve")
    public Result<Boolean> approveArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @Parameter(description = "审核人ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleService.approveArticle(id, userId));
    }

    @Operation(summary = "审核驳回", description = "驳回文章并说明原因（管理员操作）")
    @PostMapping("/{id}/reject")
    public Result<Boolean> rejectArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @Parameter(description = "审核人ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "驳回原因", required = true) @RequestParam String reason) {
        return Result.success(articleService.rejectArticle(id, userId, reason));
    }

    // ==================== 版本管理 ====================

    @Operation(summary = "获取文章版本列表", description = "获取文章的所有历史版本")
    @GetMapping("/{id}/versions")
    public Result<List<ArticleVersionVO>> getArticleVersions(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id) {
        return Result.success(articleService.getArticleVersions(id));
    }

    @Operation(summary = "搜索文章", description = "根据关键词搜索已发布文章的标题和摘要（MySQL LIKE方式）")
    @GetMapping("/search")
    public Result<PageResult<ArticleVO>> searchArticles(
            @Parameter(description = "搜索关键词", required = true, example = "Spring Boot") @RequestParam String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(articleService.searchArticles(keyword, page, size));
    }

    @Operation(summary = "回滚到历史版本", description = "将文章内容回滚到指定版本，回滚后重新进入审核")
    @PostMapping("/{id}/versions/{versionId}/rollback")
    public Result<ArticleVO> rollbackToVersion(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @Parameter(description = "版本ID", required = true) @PathVariable Long versionId,
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleService.rollbackToVersion(id, versionId, userId));
    }

}
