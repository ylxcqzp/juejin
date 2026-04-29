package com.juejin.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.CategoryDTO;
import com.juejin.content.entity.Category;
import com.juejin.content.vo.CategoryVO;

import java.util.List;

/**
 * 分类Service接口
 *
 * @author juejin
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取全部启用的分类列表
     *
     * @return 分类VO列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 分页查询分类列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<CategoryVO> getCategoryList(Integer page, Integer size);

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类VO
     */
    CategoryVO getCategoryById(Long id);

    /**
     * 创建分类
     *
     * @param dto 分类信息
     * @return 创建的分类VO
     */
    CategoryVO createCategory(CategoryDTO dto);

    /**
     * 更新分类
     *
     * @param id  分类ID
     * @param dto 分类信息
     * @return 更新后的分类VO
     */
    CategoryVO updateCategory(Long id, CategoryDTO dto);

    /**
     * 删除分类（逻辑删除）
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

}
