package com.juejin.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.TagDTO;
import com.juejin.content.entity.Tag;
import com.juejin.content.mapper.TagMapper;
import com.juejin.content.service.TagService;
import com.juejin.content.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<TagVO> getAllTags() {
        // 查询所有启用的标签，按排序值升序
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getStatus, 1)
               .orderByAsc(Tag::getSortOrder);
        List<Tag> tags = tagMapper.selectList(wrapper);
        return tags.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<TagVO> getTagList(Integer page, Integer size) {
        // 分页查询，按排序值升序
        Page<Tag> pageParam = new Page<>(page, size);
        lambdaQuery()
                .orderByAsc(Tag::getSortOrder)
                .orderByDesc(Tag::getCreateTime)
                .page(pageParam);

        List<TagVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public TagVO getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        return convertToVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO createTag(TagDTO dto) {
        // 检查标签名称是否已存在
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, dto.getName());
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.TAG_ALREADY_EXISTS);
        }

        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setDescription(dto.getDescription());
        tag.setIcon(dto.getIcon());
        tag.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        tag.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        tag.setLevel(1);
        tag.setArticleCount(0);
        tag.setFollowCount(0);

        save(tag);
        log.info("Tag created: {}, name: {}", tag.getId(), tag.getName());

        return convertToVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagVO updateTag(Long id, TagDTO dto) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        // 检查名称是否与其他标签冲突
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, dto.getName())
               .ne(Tag::getId, id);
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.TAG_ALREADY_EXISTS);
        }

        tag.setName(dto.getName());
        if (dto.getDescription() != null) {
            tag.setDescription(dto.getDescription());
        }
        if (dto.getIcon() != null) {
            tag.setIcon(dto.getIcon());
        }
        if (dto.getSortOrder() != null) {
            tag.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            tag.setStatus(dto.getStatus());
        }

        updateById(tag);
        log.info("Tag updated: {}, name: {}", tag.getId(), tag.getName());

        return convertToVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        boolean result = removeById(id);
        log.info("Tag deleted: {}, name: {}", id, tag.getName());
        return result;
    }

    /**
     * 将实体转换为VO
     */
    private TagVO convertToVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }

}
