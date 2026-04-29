package com.juejin.social.service.impl;

import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.mapper.PinMapper;
import com.juejin.social.service.FeedService;
import com.juejin.social.vo.FeedVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Feed流Service实现类
 * 初期方案：通过连表SQL查询关注用户的文章和沸点，按时间倒序混合展示
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final PinMapper pinMapper;
    private final UserFeignClient userFeignClient;

    @Override
    public PageResult<FeedVO> getFollowingFeed(Long userId, Integer page, Integer size) {
        // 简化实现：查询关注用户的沸点
        // 注：完整实现需要通过 content-service Feign 查询关注用户的文章
        int offset = (page - 1) * size;
        List<FeedVO> feedList = new ArrayList<>();

        // 查询关注用户的沸点
        var pins = pinMapper.selectFollowingPins(userId, offset, size);
        for (var pin : pins) {
            FeedVO vo = new FeedVO();
            vo.setFeedType("pin");
            vo.setContentId(pin.getId());
            vo.setUserId(pin.getUserId());
            vo.setTitle(pin.getContent().length() > 100 ? pin.getContent().substring(0, 100) : pin.getContent());
            vo.setSummary(pin.getContent());
            vo.setLikeCount(pin.getLikeCount());
            vo.setCommentCount(pin.getCommentCount());
            vo.setPublishTime(pin.getCreateTime());
            vo.setSource("following");
            fillUserInfo(vo, pin.getUserId());
            feedList.add(vo);
        }

        return PageResult.of(feedList, (long) feedList.size(), page, size);
    }

    @Override
    public PageResult<FeedVO> getRecommendFeed(Integer page, Integer size) {
        // 推荐流：全站最近的热门沸点（初期简化）
        int offset = (page - 1) * size;
        List<FeedVO> feedList = new ArrayList<>();

        var hotPins = pinMapper.selectHotPins(size);
        for (var pin : hotPins) {
            FeedVO vo = new FeedVO();
            vo.setFeedType("pin");
            vo.setContentId(pin.getId());
            vo.setUserId(pin.getUserId());
            vo.setTitle(pin.getContent().length() > 100 ? pin.getContent().substring(0, 100) : pin.getContent());
            vo.setSummary(pin.getContent());
            vo.setLikeCount(pin.getLikeCount());
            vo.setCommentCount(pin.getCommentCount());
            vo.setPublishTime(pin.getCreateTime());
            vo.setSource("recommend");
            fillUserInfo(vo, pin.getUserId());
            feedList.add(vo);
        }

        return PageResult.of(feedList, (long) feedList.size(), page, size);
    }

    @Override
    public PageResult<FeedVO> getHotFeed(Integer page, Integer size) {
        // 热门流等同于推荐流（初期实现）
        return getRecommendFeed(page, size);
    }

    /**
     * 填充用户昵称和头像
     */
    private void fillUserInfo(FeedVO vo, Long userId) {
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(userId);
            if (result != null && result.getData() != null) {
                vo.setUserNickname(result.getData().getNickname());
                vo.setUserAvatar(result.getData().getAvatar());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch feed user info: {}", e.getMessage());
        }
    }

}
