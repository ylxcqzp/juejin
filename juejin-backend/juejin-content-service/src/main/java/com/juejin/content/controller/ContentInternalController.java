package com.juejin.content.controller;

import com.juejin.common.result.Result;
import com.juejin.content.mapper.ArticleMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 内容服务内部接口（供其他微服务通过 Feign 调用，不对外暴露）
 * 用于更新文章的各种计数器（点赞数、评论数、收藏数等）
 *
 * @author juejin
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/internal/articles")
@RequiredArgsConstructor
public class ContentInternalController {

    private final ArticleMapper articleMapper;

    @PostMapping("/{articleId}/like/increment")
    public Result<Void> incrementLikeCount(@PathVariable Long articleId) {
        articleMapper.incrementLikeCount(articleId);
        return Result.success();
    }

    @PostMapping("/{articleId}/like/decrement")
    public Result<Void> decrementLikeCount(@PathVariable Long articleId) {
        articleMapper.decrementLikeCount(articleId);
        return Result.success();
    }

    @PostMapping("/{articleId}/comment/increment")
    public Result<Void> incrementCommentCount(@PathVariable Long articleId) {
        articleMapper.incrementCommentCount(articleId);
        return Result.success();
    }

    @PostMapping("/{articleId}/comment/decrement")
    public Result<Void> decrementCommentCount(@PathVariable Long articleId) {
        articleMapper.decrementCommentCount(articleId);
        return Result.success();
    }

    @PostMapping("/{articleId}/favorite/increment")
    public Result<Void> incrementFavoriteCount(@PathVariable Long articleId) {
        articleMapper.incrementFavoriteCount(articleId);
        return Result.success();
    }

    @PostMapping("/{articleId}/favorite/decrement")
    public Result<Void> decrementFavoriteCount(@PathVariable Long articleId) {
        articleMapper.decrementFavoriteCount(articleId);
        return Result.success();
    }

}
