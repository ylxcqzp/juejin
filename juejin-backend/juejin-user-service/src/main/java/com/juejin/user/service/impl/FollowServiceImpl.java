package com.juejin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.dto.PageParam;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.vo.PageResult;
import com.juejin.user.entity.User;
import com.juejin.user.entity.UserFollow;
import com.juejin.user.mapper.FollowUserRow;
import com.juejin.user.mapper.UserFollowMapper;
import com.juejin.user.mapper.UserMapper;
import com.juejin.user.service.FollowService;
import com.juejin.user.vo.FollowStatusVO;
import com.juejin.user.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements FollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId, Long followingId) {
        if (userId.equals(followingId)) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }
        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(followingId);
        if (targetUser == null || targetUser.getStatus() != 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowingId, followingId)
               .eq(UserFollow::getStatus, 1);
        UserFollow existing = userFollowMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.ALREADY_FOLLOWING);
        }
        // 创建关注记录
        UserFollow follow = new UserFollow();
        follow.setUserId(userId);
        follow.setFollowingId(followingId);
        follow.setStatus(1);
        userFollowMapper.insert(follow);
        // 更新计数
        User follower = userMapper.selectById(userId);
        follower.setFollowingCount(follower.getFollowingCount() + 1);
        userMapper.updateById(follower);
        targetUser.setFollowerCount(targetUser.getFollowerCount() + 1);
        userMapper.updateById(targetUser);
        log.info("User {} followed {}", userId, followingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Long followingId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowingId, followingId)
               .eq(UserFollow::getStatus, 1);
        UserFollow follow = userFollowMapper.selectOne(wrapper);
        if (follow == null) {
            throw new BusinessException(ErrorCode.NOT_FOLLOWING);
        }
        follow.setStatus(0);
        userFollowMapper.updateById(follow);
        // 更新计数
        User follower = userMapper.selectById(userId);
        follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
        userMapper.updateById(follower);
        User targetUser = userMapper.selectById(followingId);
        if (targetUser != null) {
            targetUser.setFollowerCount(Math.max(0, targetUser.getFollowerCount() - 1));
            userMapper.updateById(targetUser);
        }
        log.info("User {} unfollowed {}", userId, followingId);
    }

    @Override
    public PageResult<FollowUserVO> getFollowingList(Long userId, PageParam pageParam) {
        // SQL 分页查询：避免全量数据加载到内存
        int offset = (pageParam.getPage() - 1) * pageParam.getSize();
        List<FollowUserRow> pageRows = userFollowMapper.selectFollowingList(userId, offset, pageParam.getSize());
        long total = userFollowMapper.countFollowing(userId);

        List<FollowUserVO> list = pageRows.stream().map(row -> {
            FollowUserVO vo = new FollowUserVO();
            vo.setId(row.getFollowingId());
            vo.setNickname(row.getNickname());
            vo.setAvatar(row.getAvatar());
            vo.setBio(row.getBio());
            vo.setLevel(row.getLevel());
            vo.setFollowTime(row.getCreateTime());
            vo.setIsMutualFollow(userFollowMapper.countMutualFollow(userId, row.getFollowingId()) > 0);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, total, pageParam.getPage(), pageParam.getSize());
    }

    @Override
    public PageResult<FollowUserVO> getFollowerList(Long userId, PageParam pageParam) {
        // SQL 分页查询：避免全量数据加载到内存
        int offset = (pageParam.getPage() - 1) * pageParam.getSize();
        List<FollowUserRow> pageRows = userFollowMapper.selectFollowerList(userId, offset, pageParam.getSize());
        long total = userFollowMapper.countFollowers(userId);

        List<FollowUserVO> list = pageRows.stream().map(row -> {
            FollowUserVO vo = new FollowUserVO();
            vo.setId(row.getUserId());
            vo.setNickname(row.getNickname());
            vo.setAvatar(row.getAvatar());
            vo.setBio(row.getBio());
            vo.setLevel(row.getLevel());
            vo.setFollowTime(row.getCreateTime());
            vo.setIsMutualFollow(userFollowMapper.countMutualFollow(userId, row.getUserId()) > 0);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(list, total, pageParam.getPage(), pageParam.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFollower(Long userId, Long followerId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, followerId)
               .eq(UserFollow::getFollowingId, userId)
               .eq(UserFollow::getStatus, 1);
        UserFollow follow = userFollowMapper.selectOne(wrapper);
        if (follow == null) {
            throw new BusinessException(ErrorCode.NOT_FOLLOWING);
        }
        follow.setStatus(0);
        userFollowMapper.updateById(follow);
        // 被移除方的关注数 -1
        User follower = userMapper.selectById(followerId);
        if (follower != null) {
            follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
            userMapper.updateById(follower);
        }
        // 自己的粉丝数 -1
        User self = userMapper.selectById(userId);
        if (self != null) {
            self.setFollowerCount(Math.max(0, self.getFollowerCount() - 1));
            userMapper.updateById(self);
        }
        log.info("User {} removed follower {}", userId, followerId);
    }

    @Override
    public FollowStatusVO checkFollowStatus(Long userId, Long targetId) {
        FollowStatusVO vo = new FollowStatusVO();
        // 我是否关注了对方
        LambdaQueryWrapper<UserFollow> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(UserFollow::getUserId, userId)
                .eq(UserFollow::getFollowingId, targetId)
                .eq(UserFollow::getStatus, 1);
        vo.setIsFollowing(userFollowMapper.selectCount(wrapper1) > 0);
        // 对方是否关注了我
        LambdaQueryWrapper<UserFollow> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(UserFollow::getUserId, targetId)
                .eq(UserFollow::getFollowingId, userId)
                .eq(UserFollow::getStatus, 1);
        vo.setIsFollowedBy(userFollowMapper.selectCount(wrapper2) > 0);
        vo.setIsMutualFollow(vo.getIsFollowing() && vo.getIsFollowedBy());
        return vo;
    }
}
