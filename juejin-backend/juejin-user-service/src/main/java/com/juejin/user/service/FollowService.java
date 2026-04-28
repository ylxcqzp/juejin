package com.juejin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.dto.PageParam;
import com.juejin.common.vo.PageResult;
import com.juejin.user.entity.UserFollow;
import com.juejin.user.vo.FollowStatusVO;
import com.juejin.user.vo.FollowUserVO;

public interface FollowService extends IService<UserFollow> {

    void follow(Long userId, Long followingId);

    void unfollow(Long userId, Long followingId);

    PageResult<FollowUserVO> getFollowingList(Long userId, PageParam pageParam);

    PageResult<FollowUserVO> getFollowerList(Long userId, PageParam pageParam);

    void removeFollower(Long userId, Long followerId);

    FollowStatusVO checkFollowStatus(Long userId, Long targetId);

}
