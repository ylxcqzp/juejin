package com.juejin.common.feign;

import com.juejin.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 内容服务Feign客户端（内部调用，用于更新文章计数器）
 * 认证信息由 FeignConfig 请求拦截器自动传递
 *
 * @author juejin
 */
@FeignClient(name = "juejin-content-service", path = "/internal/articles")
public interface ContentFeignClient {

    /**
     * 增加文章点赞数
     */
    @PostMapping("/{articleId}/like/increment")
    Result<Void> incrementLikeCount(@PathVariable("articleId") Long articleId);

    /**
     * 减少文章点赞数
     */
    @PostMapping("/{articleId}/like/decrement")
    Result<Void> decrementLikeCount(@PathVariable("articleId") Long articleId);

    /**
     * 增加文章评论数
     */
    @PostMapping("/{articleId}/comment/increment")
    Result<Void> incrementCommentCount(@PathVariable("articleId") Long articleId);

    /**
     * 减少文章评论数
     */
    @PostMapping("/{articleId}/comment/decrement")
    Result<Void> decrementCommentCount(@PathVariable("articleId") Long articleId);

    /**
     * 增加文章收藏数
     */
    @PostMapping("/{articleId}/favorite/increment")
    Result<Void> incrementFavoriteCount(@PathVariable("articleId") Long articleId);

    /**
     * 减少文章收藏数
     */
    @PostMapping("/{articleId}/favorite/decrement")
    Result<Void> decrementFavoriteCount(@PathVariable("articleId") Long articleId);

}
