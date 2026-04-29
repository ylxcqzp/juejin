package com.juejin.content.controller;

import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.CategoryDTO;
import com.juejin.content.service.CategoryService;
import com.juejin.content.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 *
 * @author juejin
 */
@Tag(name = "分类管理", description = "分类的查询、创建、更新、删除等相关接口")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取全部分类", description = "获取所有启用状态的分类列表")
    @GetMapping("/all")
    public Result<List<CategoryVO>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @Operation(summary = "分页查询分类", description = "分页获取分类列表")
    @GetMapping
    public Result<PageResult<CategoryVO>> getCategoryList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(categoryService.getCategoryList(page, size));
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryById(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "创建分类", description = "创建新分类（管理员操作）")
    @PostMapping
    public Result<CategoryVO> createCategory(@RequestBody @Validated CategoryDTO dto) {
        return Result.success(categoryService.createCategory(dto));
    }

    @Operation(summary = "更新分类", description = "更新已有分类（管理员操作）")
    @PutMapping("/{id}")
    public Result<CategoryVO> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id,
            @RequestBody @Validated CategoryDTO dto) {
        return Result.success(categoryService.updateCategory(id, dto));
    }

    @Operation(summary = "删除分类", description = "逻辑删除分类（管理员操作）")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        return Result.success(categoryService.deleteCategory(id));
    }

}
