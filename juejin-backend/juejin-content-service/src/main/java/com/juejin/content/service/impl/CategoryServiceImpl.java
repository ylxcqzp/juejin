package com.juejin.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.CategoryDTO;
import com.juejin.content.entity.Category;
import com.juejin.content.mapper.CategoryMapper;
import com.juejin.content.service.CategoryService;
import com.juejin.content.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        // 查询所有启用的分类，按排序值升序
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<CategoryVO> getCategoryList(Integer page, Integer size) {
        // 分页查询，按排序值升序
        Page<Category> pageParam = new Page<>(page, size);
        lambdaQuery()
                .orderByAsc(Category::getSortOrder)
                .orderByDesc(Category::getCreateTime)
                .page(pageParam);

        List<CategoryVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO createCategory(CategoryDTO dto) {
        // 检查分类名称是否已存在
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, dto.getName());
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        category.setLevel(1);
        category.setArticleCount(0);

        save(category);
        log.info("Category created: {}, name: {}", category.getId(), category.getName());

        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 检查名称是否与其他分类冲突
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, dto.getName())
               .ne(Category::getId, id);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        category.setName(dto.getName());
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }

        updateById(category);
        log.info("Category updated: {}, name: {}", category.getId(), category.getName());

        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        boolean result = removeById(id);
        log.info("Category deleted: {}, name: {}", id, category.getName());
        return result;
    }

    /**
     * 将实体转换为VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

}
