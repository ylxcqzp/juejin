package com.juejin.social.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.service.FeedService;
import com.juejin.social.vo.FeedVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Feed流控制器
 *
 * @author juejin
 */
@Tag(name = "Feed信息流", description = "关注流、推荐流、热门流相关接口")
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "关注流", description = "获取关注用户的文章和沸点")
    @GetMapping("/following")
    public Result<PageResult<FeedVO>> getFollowingFeed(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(feedService.getFollowingFeed(userId, page, size));
    }

    @Operation(summary = "推荐流", description = "获取个性化推荐内容")
    @GetMapping("/recommend")
    public Result<PageResult<FeedVO>> getRecommendFeed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(feedService.getRecommendFeed(page, size));
    }

    @Operation(summary = "热门流", description = "获取全站热门内容")
    @GetMapping("/hot")
    public Result<PageResult<FeedVO>> getHotFeed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(feedService.getHotFeed(page, size));
    }

}
