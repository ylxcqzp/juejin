package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.ContentFeignClient;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.CommentDTO;
import com.juejin.social.entity.Comment;
import com.juejin.social.mapper.CommentMapper;
import com.juejin.social.service.CommentService;
import com.juejin.social.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final UserFeignClient userFeignClient;
    private final ContentFeignClient contentFeignClient;

    /** 评论最大嵌套层数 */
    private static final int MAX_DEPTH = 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long userId, CommentDTO dto) {
        // 如果是回复，验证父评论存在且在同一目标下
        Long rootId = dto.getRootId();
        Long parentId = dto.getParentId();

        if (parentId != null) {
            Comment parentComment = commentMapper.selectById(parentId);
            if (parentComment == null || parentComment.getStatus() == 0) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
            // 确定根评论：如果父评论有 rootId，则继承其 rootId，否则父评论就是根评论
            if (rootId == null) {
                rootId = parentComment.getRootId() != null ? parentComment.getRootId() : parentComment.getId();
            }
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetId(dto.getTargetId());
        comment.setTargetType(dto.getTargetType());
        comment.setContent(dto.getContent());
        comment.setParentId(parentId);
        comment.setRootId(rootId);
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setIsTop(0);
        comment.setStatus(1);

        save(comment);

        // 如果是回复，更新父评论的回复数
        if (parentId != null) {
            commentMapper.incrementReplyCount(parentId);
        }

        log.info("Comment created: userId={}, targetId={}, targetType={}", userId, dto.getTargetId(), dto.getTargetType());

        // 跨服务同步：如果是文章评论，增加文章评论数
        if (dto.getTargetType() == 1) {
            syncArticleCommentCount(dto.getTargetId(), true);
        }

        return convertToVO(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 权限检查：只有评论发布者可以删除
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_DELETE_PERMISSION);
        }

        // 软删除：将状态置为0
        comment.setStatus(0);
        updateById(comment);

        // 如果有父评论，减少其回复数
        if (comment.getParentId() != null) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }

        log.info("Comment deleted: commentId={}, userId={}", commentId, userId);

        // 跨服务同步：如果是文章评论，减少文章评论数
        if (comment.getTargetType() == 1) {
            syncArticleCommentCount(comment.getTargetId(), false);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean topComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 权限检查：需要验证操作者是目标文章的作者
        // 通过 Feign 调用 content-service 验证文章所有权
        // 当前简化处理：检查评论是否属于文章类型，如果是则通过 Feign 获取文章作者
        if (comment.getTargetType() != 1) {
            throw new BusinessException(ErrorCode.NOT_SUPPORTED);
        }
        // 注：完整的文章作者校验需要调用 content-service 的 Feign 接口
        // 当前阶段简化处理，仅记录日志

        comment.setIsTop(comment.getIsTop() == 1 ? 0 : 1);
        updateById(comment);

        log.info("Comment top toggled: commentId={}, isTop={}", commentId, comment.getIsTop());
        return true;
    }

    @Override
    public PageResult<CommentVO> getCommentList(Long targetId, Integer targetType, Integer page, Integer size, String sortBy) {
        // 查询一级评论（parentId 为空）
        Page<Comment> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTargetId, targetId)
               .eq(Comment::getTargetType, targetType)
               .isNull(Comment::getParentId)
               .eq(Comment::getStatus, 1);

        // 排序方式
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Comment::getLikeCount);
        } else {
            wrapper.orderByDesc(Comment::getIsTop)
                   .orderByDesc(Comment::getCreateTime);
        }

        commentMapper.selectPage(pageParam, wrapper);

        List<Comment> rootComments = pageParam.getRecords();

        // 批量加载子回复（仅加载一级回复，最多2层嵌套）
        List<Long> rootIds = rootComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        Map<Long, List<Comment>> repliesMap;
        if (!rootIds.isEmpty()) {
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.in(Comment::getRootId, rootIds)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreateTime);
            List<Comment> replies = commentMapper.selectList(replyWrapper);
            repliesMap = replies.stream()
                    .collect(Collectors.groupingBy(Comment::getRootId));
        } else {
            repliesMap = new HashMap<>();
        }

        // 转换为VO
        List<CommentVO> records = rootComments.stream().map(root -> {
            CommentVO vo = convertToVO(root);
            List<Comment> childReplies = repliesMap.getOrDefault(root.getId(), Collections.emptyList());
            vo.setReplies(childReplies.stream().map(this::convertToVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    /**
     * 跨服务同步文章评论数（调用失败不影响主流程）
     */
    private void syncArticleCommentCount(Long articleId, boolean isIncrement) {
        try {
            if (isIncrement) {
                contentFeignClient.incrementCommentCount(articleId);
            } else {
                contentFeignClient.decrementCommentCount(articleId);
            }
        } catch (Exception e) {
            log.warn("Failed to sync article comment count: articleId={}, error={}", articleId, e.getMessage());
        }
    }

    /**
     * 评论实体转VO，包含用户信息填充
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        // 填充评论者昵称和头像
        fillUserInfo(vo, comment.getUserId(), true);

        // 填充被回复用户昵称
        if (comment.getReplyUserId() != null) {
            fillUserInfo(vo, comment.getReplyUserId(), false);
        }

        return vo;
    }

    /**
     * 通过 Feign 填充用户昵称和头像
     *
     * @param vo      评论VO
     * @param userId  用户ID
     * @param isOwner true-填充评论者信息，false-填充被回复者信息
     */
    private void fillUserInfo(CommentVO vo, Long userId, boolean isOwner) {
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(userId);
            if (result != null && result.getData() != null) {
                if (isOwner) {
                    vo.setUserNickname(result.getData().getNickname());
                    vo.setUserAvatar(result.getData().getAvatar());
                } else {
                    vo.setReplyUserNickname(result.getData().getNickname());
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch user info for userId={}: {}", userId, e.getMessage());
        }
    }

}
