package com.juejin.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.TagDTO;
import com.juejin.content.entity.Tag;
import com.juejin.content.vo.TagVO;

import java.util.List;

/**
 * 标签Service接口
 *
 * @author juejin
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取全部启用的标签列表
     *
     * @return 标签VO列表
     */
    List<TagVO> getAllTags();

    /**
     * 分页查询标签列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<TagVO> getTagList(Integer page, Integer size);

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签VO
     */
    TagVO getTagById(Long id);

    /**
     * 创建标签
     *
     * @param dto 标签信息
     * @return 创建的标签VO
     */
    TagVO createTag(TagDTO dto);

    /**
     * 更新标签
     *
     * @param id  标签ID
     * @param dto 标签信息
     * @return 更新后的标签VO
     */
    TagVO updateTag(Long id, TagDTO dto);

    /**
     * 删除标签（逻辑删除）
     *
     * @param id 标签ID
     * @return 是否成功
     */
    boolean deleteTag(Long id);

}
