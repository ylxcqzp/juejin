package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.ContentFeignClient;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import com.juejin.social.dto.LikeDTO;
import com.juejin.social.entity.LikeRecord;
import com.juejin.social.mapper.CommentMapper;
import com.juejin.social.mapper.LikeRecordMapper;
import com.juejin.social.service.LikeService;
import com.juejin.social.vo.LikeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点赞Service实现类
 * 点赞和取消点赞通过切换状态实现（非物理删除）
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final CommentMapper commentMapper;
    private final UserFeignClient userFeignClient;
    private final ContentFeignClient contentFeignClient;

    private static final int LIKE_RATE_LIMIT_SECONDS = 60;
    private static final String RATE_LIMIT_KEY_PREFIX = "like:rate:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeVO toggleLike(Long userId, LikeDTO dto) {
        Long targetId = dto.getTargetId();
        Integer targetType = dto.getTargetType();

        // 查询是否已有点赞记录（包括已取消的）
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
               .eq(LikeRecord::getTargetId, targetId)
               .eq(LikeRecord::getTargetType, targetType);
        LikeRecord record = likeRecordMapper.selectOne(wrapper);

        if (record != null) {
            // 已有记录：切换点赞状态
            if (record.getStatus() == 1) {
                // 当前已点赞 → 取消点赞
                record.setStatus(0);
                updateById(record);
                log.info("Like cancelled: userId={}, targetId={}, targetType={}", userId, targetId, targetType);
            } else {
                // 当前未点赞 → 重新点赞
                record.setStatus(1);
                updateById(record);
                log.info("Like re-activated: userId={}, targetId={}, targetType={}", userId, targetId, targetType);
            }
        } else {
            // 新记录：首次点赞
            record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetId(targetId);
            record.setTargetType(targetType);
            record.setStatus(1);
            save(record);
            log.info("Like created: userId={}, targetId={}, targetType={}", userId, targetId, targetType);
        }

        // 构建返回VO
        LikeVO vo = new LikeVO();
        vo.setId(record.getId());
        vo.setUserId(userId);
        vo.setTargetId(targetId);
        vo.setTargetType(targetType);
        vo.setCreateTime(record.getCreateTime());
        vo.setIsLiked(record.getStatus() == 1);
        vo.setLikeCount(likeRecordMapper.countLikes(targetId, targetType));

        // 跨服务同步计数器
        syncCounter(targetId, targetType, record.getStatus() == 1);

        return vo;
    }

    /**
     * 跨服务同步计数器（异步调用，失败不影响主流程）
     * - 文章点赞：通过 Feign 更新 content-service 的 article.like_count
     * - 评论点赞：直接更新本模块的 comment.like_count
     */
    private void syncCounter(Long targetId, Integer targetType, boolean isIncrement) {
        try {
            if (targetType == 1) {
                // 点赞文章：同步文章点赞数
                if (isIncrement) {
                    contentFeignClient.incrementLikeCount(targetId);
                } else {
                    contentFeignClient.decrementLikeCount(targetId);
                }
            } else if (targetType == 2) {
                // 点赞评论：同步评论点赞数
                if (isIncrement) {
                    commentMapper.incrementLikeCount(targetId);
                } else {
                    commentMapper.decrementLikeCount(targetId);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to sync counter: targetId={}, targetType={}, isIncrement={}, error={}",
                    targetId, targetType, isIncrement, e.getMessage());
        }
    }

    @Override
    public List<LikeVO> getLikeList(Long targetId, Integer targetType, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Long> userIds = likeRecordMapper.selectRecentLikeUserIds(targetId, targetType, size);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        long totalLikes = likeRecordMapper.countLikes(targetId, targetType);

        // 批量查询用户信息
        return userIds.stream().map(uid -> {
            LikeVO vo = new LikeVO();
            vo.setUserId(uid);
            vo.setTargetId(targetId);
            vo.setTargetType(targetType);
            vo.setIsLiked(true);
            vo.setLikeCount(totalLikes);
            // 填充用户昵称和头像
            fillUserInfo(vo, uid);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, Integer targetType) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
               .eq(LikeRecord::getTargetId, targetId)
               .eq(LikeRecord::getTargetType, targetType)
               .eq(LikeRecord::getStatus, 1);
        return likeRecordMapper.selectCount(wrapper) > 0;
    }

    /**
     * 通过 Feign 填充用户信息，失败时静默降级
     */
    private void fillUserInfo(LikeVO vo, Long userId) {
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(userId);
            if (result != null && result.getData() != null) {
                vo.setUserNickname(result.getData().getNickname());
                vo.setUserAvatar(result.getData().getAvatar());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch user info for userId={}: {}", userId, e.getMessage());
        }
    }

}
