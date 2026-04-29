package com.juejin.social.service;

import com.juejin.common.vo.PageResult;
import com.juejin.social.vo.FeedVO;

/**
 * Feed流Service接口
 *
 * @author juejin
 */
public interface FeedService {

    /**
     * 获取关注流（关注用户的文章和沸点）
     */
    PageResult<FeedVO> getFollowingFeed(Long userId, Integer page, Integer size);

    /**
     * 获取推荐流（全站热门内容）
     */
    PageResult<FeedVO> getRecommendFeed(Integer page, Integer size);

    /**
     * 获取热门流（全站热门文章+沸点，Hacker News排名算法）
     */
    PageResult<FeedVO> getHotFeed(Integer page, Integer size);

}
