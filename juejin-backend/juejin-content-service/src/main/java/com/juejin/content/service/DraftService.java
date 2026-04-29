package com.juejin.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.content.dto.DraftDTO;
import com.juejin.content.entity.Draft;
import com.juejin.content.vo.DraftVO;

/**
 * 草稿Service接口
 *
 * @author juejin
 */
public interface DraftService extends IService<Draft> {

    /**
     * 分页查询用户草稿列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 分页结果
     */
    PageResult<DraftVO> getDraftList(Long userId, Integer page, Integer size);

    /**
     * 获取草稿详情
     *
     * @param userId  用户ID
     * @param draftId 草稿ID
     * @return 草稿VO
     */
    DraftVO getDraftById(Long userId, Long draftId);

    /**
     * 保存草稿（新建或自动保存）
     *
     * @param userId 用户ID
     * @param dto    草稿内容
     * @return 草稿VO
     */
    DraftVO saveDraft(Long userId, DraftDTO dto);

    /**
     * 自动保存草稿（定时调用，与 saveDraft 逻辑相同但不抛异常）
     *
     * @param userId  用户ID
     * @param draftId 已有草稿ID（可选）
     * @param dto     草稿内容
     * @return 草稿VO
     */
    DraftVO autoSave(Long userId, Long draftId, DraftDTO dto);

    /**
     * 删除草稿
     *
     * @param userId  用户ID
     * @param draftId 草稿ID
     * @return 是否成功
     */
    boolean deleteDraft(Long userId, Long draftId);

    /**
     * 从草稿发布文章
     *
     * @param userId  用户ID
     * @param draftId 草稿ID
     * @param dto     补充的发布信息（分类、标签等）
     * @return 发布的文章ID
     */
    Long publishFromDraft(Long userId, Long draftId, DraftDTO dto);

}
