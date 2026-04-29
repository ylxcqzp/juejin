package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.CommentDTO;
import com.juejin.social.entity.Comment;
import com.juejin.social.vo.CommentVO;

/**
 * 评论Service接口
 *
 * @author juejin
 */
public interface CommentService extends IService<Comment> {

    /**
     * 发表评论或回复
     *
     * @param userId 用户ID
     * @param dto    评论信息
     * @return 评论VO
     */
    CommentVO createComment(Long userId, CommentDTO dto);

    /**
     * 删除评论（软删除，作者或评论发布者可操作）
     *
     * @param userId    操作人ID
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long userId, Long commentId);

    /**
     * 置顶评论（仅文章作者可操作）
     *
     * @param userId    操作人ID
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean topComment(Long userId, Long commentId);

    /**
     * 分页查询目标的评论列表（展示一级评论及其子回复）
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param page       页码
     * @param size       每页数量
     * @param sortBy     排序方式：time-最新 hot-最热
     * @return 评论列表（含嵌套回复）
     */
    PageResult<CommentVO> getCommentList(Long targetId, Integer targetType, Integer page, Integer size, String sortBy);

}
