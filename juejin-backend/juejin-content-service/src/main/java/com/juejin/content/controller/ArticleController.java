package com.juejin.content.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.ArticleCreateDTO;
import com.juejin.content.dto.ArticleUpdateDTO;
import com.juejin.content.service.ArticleService;
import com.juejin.content.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章Controller
 *
 * @author juejin
 */
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 创建文章
     */
    @PostMapping
    public Result<ArticleVO> createArticle(
            @RequestBody @Validated ArticleCreateDTO dto,
            @RequestHeader("X-User-Id") Long userId) {
        ArticleVO articleVO = articleService.createArticle(dto, userId);
        return Result.success(articleVO);
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result<ArticleVO> updateArticle(
            @PathVariable Long id,
            @RequestBody @Validated ArticleUpdateDTO dto,
            @RequestHeader("X-User-Id") Long userId) {
        dto.setId(id);
        ArticleVO articleVO = articleService.updateArticle(dto, userId);
        return Result.success(articleVO);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleById(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getArticleById(id);
        return Result.success(articleVO);
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteArticle(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        boolean result = articleService.deleteArticle(id, userId);
        return Result.success(result);
    }

    /**
     * 获取文章列表
     */
    @GetMapping
    public Result<PageResult<ArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId) {
        PageResult<ArticleVO> result = articleService.getArticleList(page, size, categoryId);
        return Result.success(result);
    }

    /**
     * 获取用户文章列表
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<ArticleVO>> getUserArticles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ArticleVO> result = articleService.getUserArticles(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public Result<List<ArticleVO>> getHotArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ArticleVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

}
