package com.juejin.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.ArticleCreateDTO;
import com.juejin.content.dto.DraftDTO;
import com.juejin.content.entity.Draft;
import com.juejin.content.mapper.DraftMapper;
import com.juejin.content.service.ArticleService;
import com.juejin.content.service.DraftService;
import com.juejin.content.vo.ArticleVO;
import com.juejin.content.vo.DraftVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 草稿Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DraftServiceImpl extends ServiceImpl<DraftMapper, Draft> implements DraftService {

    private final DraftMapper draftMapper;
    private final ArticleService articleService;

    /** 草稿最大数量 */
    private static final int MAX_DRAFT_COUNT = 50;

    @Override
    public PageResult<DraftVO> getDraftList(Long userId, Integer page, Integer size) {
        Page<Draft> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Draft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Draft::getUserId, userId)
               .orderByDesc(Draft::getUpdateTime);
        draftMapper.selectPage(pageParam, wrapper);

        List<DraftVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public DraftVO getDraftById(Long userId, Long draftId) {
        Draft draft = draftMapper.selectById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DRAFT_NOT_FOUND);
        }
        return convertToVO(draft);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DraftVO saveDraft(Long userId, DraftDTO dto) {
        // 检查草稿数量上限
        long count = draftMapper.countByUserId(userId);
        if (count >= MAX_DRAFT_COUNT) {
            throw new BusinessException(ErrorCode.DRAFT_LIMIT_EXCEEDED);
        }

        Draft draft = new Draft();
        draft.setUserId(userId);
        draft.setTitle(dto.getTitle());
        draft.setContent(dto.getContent());
        draft.setCoverImage(dto.getCoverImage());
        draft.setTags(dto.getTags());
        draft.setCategoryId(dto.getCategoryId());
        draft.setAutoSaveTime(LocalDateTime.now());

        save(draft);
        log.info("Draft saved: userId={}, draftId={}", userId, draft.getId());
        return convertToVO(draft);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DraftVO autoSave(Long userId, Long draftId, DraftDTO dto) {
        Draft draft;
        if (draftId != null) {
            draft = draftMapper.selectById(draftId);
            if (draft == null || !draft.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.DRAFT_NOT_FOUND);
            }
        } else {
            // 检查草稿数量上限
            long count = draftMapper.countByUserId(userId);
            if (count >= MAX_DRAFT_COUNT) {
                throw new BusinessException(ErrorCode.DRAFT_LIMIT_EXCEEDED);
            }
            draft = new Draft();
            draft.setUserId(userId);
        }

        if (dto.getTitle() != null) draft.setTitle(dto.getTitle());
        if (dto.getContent() != null) draft.setContent(dto.getContent());
        if (dto.getCoverImage() != null) draft.setCoverImage(dto.getCoverImage());
        if (dto.getTags() != null) draft.setTags(dto.getTags());
        if (dto.getCategoryId() != null) draft.setCategoryId(dto.getCategoryId());
        draft.setAutoSaveTime(LocalDateTime.now());

        if (draft.getId() == null) {
            save(draft);
        } else {
            updateById(draft);
        }
        return convertToVO(draft);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDraft(Long userId, Long draftId) {
        Draft draft = draftMapper.selectById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DRAFT_NOT_FOUND);
        }
        removeById(draftId);
        log.info("Draft deleted: userId={}, draftId={}", userId, draftId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishFromDraft(Long userId, Long draftId, DraftDTO dto) {
        Draft draft = draftMapper.selectById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DRAFT_NOT_FOUND);
        }

        // 使用合并后的信息：dto 中传入的优先，否则使用草稿中的
        ArticleCreateDTO articleDTO = new ArticleCreateDTO();
        articleDTO.setTitle(dto.getTitle() != null ? dto.getTitle() : draft.getTitle());
        articleDTO.setContent(dto.getContent() != null ? dto.getContent() : draft.getContent());
        articleDTO.setCoverImage(dto.getCoverImage() != null ? dto.getCoverImage() : draft.getCoverImage());
        articleDTO.setCategoryId(dto.getCategoryId() != null ? dto.getCategoryId() : draft.getCategoryId());
        articleDTO.setIsOriginal(1);

        // 解析标签
        if (dto.getTags() != null) {
            // dto 中的标签（JSON格式）
        }

        ArticleVO article = articleService.createArticle(articleDTO, userId);

        // 删除草稿（发布后草稿不再需要）
        removeById(draftId);
        log.info("Article published from draft: userId={}, draftId={}, articleId={}", userId, draftId, article.getId());

        return article.getId();
    }

    /**
     * 草稿实体转VO
     */
    private DraftVO convertToVO(Draft draft) {
        DraftVO vo = new DraftVO();
        BeanUtils.copyProperties(draft, vo);
        return vo;
    }

}
