package com.juejin.social.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.PinDTO;
import com.juejin.social.service.PinService;
import com.juejin.social.vo.PinTopicVO;
import com.juejin.social.vo.PinVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 沸点控制器
 *
 * @author juejin
 */
@Tag(name = "沸点管理", description = "沸点发布、浏览、话题相关接口")
@RestController
@RequestMapping("/api/v1/pins")
@RequiredArgsConstructor
@Validated
public class PinController {

    private final PinService pinService;

    @Operation(summary = "发布沸点", description = "发布一条沸点动态")
    @PostMapping
    public Result<PinVO> createPin(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Validated PinDTO dto) {
        return Result.success(pinService.createPin(userId, dto));
    }

    @Operation(summary = "沸点列表", description = "分页查询沸点列表，支持按最新/最热排序")
    @GetMapping
    public Result<PageResult<PinVO>> getPinList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "time") String sortBy) {
        return Result.success(pinService.getPinList(page, size, sortBy));
    }

    @Operation(summary = "关注流沸点", description = "查询关注用户的沸点列表")
    @GetMapping("/following")
    public Result<PageResult<PinVO>> getFollowingPins(
            @Parameter(description = "用户ID", hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pinService.getFollowingPins(userId, page, size));
    }

    @Operation(summary = "话题沸点", description = "查询指定话题下的沸点列表")
    @GetMapping("/topic/{topicId}")
    public Result<PageResult<PinVO>> getTopicPins(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pinService.getTopicPins(topicId, page, size));
    }

    @Operation(summary = "热门话题", description = "查询热门话题列表")
    @GetMapping("/topics/hot")
    public Result<List<PinTopicVO>> getHotTopics(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(pinService.getHotTopics(limit));
    }

}
