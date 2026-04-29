package com.juejin.content.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.TagDTO;
import com.juejin.content.service.TagService;
import com.juejin.content.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 *
 * @author juejin
 */
@Tag(name = "标签管理", description = "标签的查询、创建、更新、删除等相关接口")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;

    @Operation(summary = "获取全部标签", description = "获取所有启用状态的标签列表")
    @GetMapping("/all")
    public Result<List<TagVO>> getAllTags() {
        return Result.success(tagService.getAllTags());
    }

    @Operation(summary = "分页查询标签", description = "分页获取标签列表")
    @GetMapping
    public Result<PageResult<TagVO>> getTagList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(tagService.getTagList(page, size));
    }

    @Operation(summary = "获取标签详情", description = "根据ID获取标签详细信息")
    @GetMapping("/{id}")
    public Result<TagVO> getTagById(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id) {
        return Result.success(tagService.getTagById(id));
    }

    @Operation(summary = "创建标签", description = "创建新标签（管理员操作）")
    @PostMapping
    public Result<TagVO> createTag(@RequestBody @Validated TagDTO dto) {
        return Result.success(tagService.createTag(dto));
    }

    @Operation(summary = "更新标签", description = "更新已有标签（管理员操作）")
    @PutMapping("/{id}")
    public Result<TagVO> updateTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id,
            @RequestBody @Validated TagDTO dto) {
        return Result.success(tagService.updateTag(id, dto));
    }

    @Operation(summary = "删除标签", description = "逻辑删除标签（管理员操作）")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id) {
        return Result.success(tagService.deleteTag(id));
    }

}
