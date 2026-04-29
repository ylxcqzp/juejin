package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.PinDTO;
import com.juejin.social.entity.Pin;
import com.juejin.social.vo.PinVO;

import java.util.List;

/**
 * 沸点Service接口
 *
 * @author juejin
 */
public interface PinService extends IService<Pin> {

    /**
     * 发布沸点
     */
    PinVO createPin(Long userId, PinDTO dto);

    /**
     * 分页查询沸点列表
     */
    PageResult<PinVO> getPinList(Integer page, Integer size, String sortBy);

    /**
     * 查询关注用户的沸点流
     */
    PageResult<PinVO> getFollowingPins(Long userId, Integer page, Integer size);

    /**
     * 查询话题下的沸点
     */
    PageResult<PinVO> getTopicPins(Long topicId, Integer page, Integer size);

    /**
     * 查询热门话题
     */
    List<com.juejin.social.vo.PinTopicVO> getHotTopics(int limit);

}
