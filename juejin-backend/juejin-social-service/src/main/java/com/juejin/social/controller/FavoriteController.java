package com.juejin.social.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.FavoriteFolderDTO;
import com.juejin.social.dto.FavoriteRecordDTO;
import com.juejin.social.service.FavoriteService;
import com.juejin.social.vo.FavoriteFolderVO;
import com.juejin.social.vo.FavoriteRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器（收藏夹管理 + 收藏操作）
 *
 * @author juejin
 */
@Tag(name = "收藏管理", description = "收藏夹管理、收藏/取消收藏相关接口")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Validated
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ==================== 收藏夹管理 ====================

    @Operation(summary = "获取收藏夹列表", description = "获取当前用户的所有收藏夹")
    @GetMapping("/folders")
    public Result<List<FavoriteFolderVO>> getFolders(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(favoriteService.getFolders(userId));
    }

    @Operation(summary = "创建收藏夹", description = "创建新的收藏夹，最多20个")
    @PostMapping("/folders")
    public Result<FavoriteFolderVO> createFolder(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated FavoriteFolderDTO dto) {
        return Result.success(favoriteService.createFolder(userId, dto));
    }

    @Operation(summary = "更新收藏夹", description = "更新收藏夹信息")
    @PutMapping("/folders/{folderId}")
    public Result<FavoriteFolderVO> updateFolder(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "收藏夹ID", required = true) @PathVariable Long folderId,
            @RequestBody @Validated FavoriteFolderDTO dto) {
        return Result.success(favoriteService.updateFolder(userId, folderId, dto));
    }

    @Operation(summary = "删除收藏夹", description = "删除收藏夹，不能删除默认收藏夹")
    @DeleteMapping("/folders/{folderId}")
    public Result<Boolean> deleteFolder(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "收藏夹ID", required = true) @PathVariable Long folderId) {
        return Result.success(favoriteService.deleteFolder(userId, folderId));
    }

    // ==================== 收藏操作 ====================

    @Operation(summary = "收藏文章", description = "收藏文章到指定收藏夹，不传收藏夹ID则收藏到默认收藏夹")
    @PostMapping
    public Result<FavoriteRecordVO> addFavorite(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated FavoriteRecordDTO dto) {
        return Result.success(favoriteService.addFavorite(userId, dto));
    }

    @Operation(summary = "取消收藏", description = "取消对文章的收藏")
    @DeleteMapping("/{articleId}")
    public Result<Boolean> removeFavorite(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        return Result.success(favoriteService.removeFavorite(userId, articleId));
    }

    @Operation(summary = "查询收藏夹文章列表", description = "分页查询收藏夹中的文章")
    @GetMapping("/folders/{folderId}/articles")
    public Result<PageResult<FavoriteRecordVO>> getFavoritesByFolder(
            @Parameter(description = "收藏夹ID", required = true) @PathVariable Long folderId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(favoriteService.getFavoritesByFolder(folderId, page, size));
    }

    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏某文章")
    @GetMapping("/status")
    public Result<Boolean> checkFavoriteStatus(
            @Parameter(description = "用户ID", hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "文章ID", required = true) @RequestParam Long articleId) {
        return Result.success(favoriteService.isFavorited(userId, articleId));
    }

}
