package com.juejin.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.feign.UserFeignClient;
import com.juejin.common.feign.UserInfoVO;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.PinDTO;
import com.juejin.social.entity.Pin;
import com.juejin.social.entity.PinTopic;
import com.juejin.social.mapper.PinMapper;
import com.juejin.social.mapper.PinTopicMapper;
import com.juejin.social.service.PinService;
import com.juejin.social.vo.PinTopicVO;
import com.juejin.social.vo.PinVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 沸点Service实现类
 *
 * @author juejin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PinServiceImpl extends ServiceImpl<PinMapper, Pin> implements PinService {

    private final PinMapper pinMapper;
    private final PinTopicMapper pinTopicMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PinVO createPin(Long userId, PinDTO dto) {
        Pin pin = new Pin();
        pin.setUserId(userId);
        pin.setContent(dto.getContent());
        pin.setImages(dto.getImages());
        pin.setLinkUrl(dto.getLinkUrl());
        pin.setLinkTitle(dto.getLinkTitle());
        pin.setLinkCover(dto.getLinkCover());
        pin.setTopicId(dto.getTopicId());
        pin.setLikeCount(0);
        pin.setCommentCount(0);
        pin.setShareCount(0);
        pin.setIsHot(0);
        pin.setStatus(1);

        save(pin);

        // 更新话题沸点数
        if (dto.getTopicId() != null) {
            pinTopicMapper.incrementPinCount(dto.getTopicId());
        }

        log.info("Pin created: userId={}, pinId={}", userId, pin.getId());
        return convertToVO(pin);
    }

    @Override
    public PageResult<PinVO> getPinList(Integer page, Integer size, String sortBy) {
        Page<Pin> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pin::getStatus, 1);

        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Pin::getLikeCount).orderByDesc(Pin::getCreateTime);
        } else {
            wrapper.orderByDesc(Pin::getCreateTime);
        }

        pinMapper.selectPage(pageParam, wrapper);
        List<PinVO> records = pageParam.getRecords().stream()
                .map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(records, pageParam.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public PageResult<PinVO> getFollowingPins(Long userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Pin> pins = pinMapper.selectFollowingPins(userId, offset, size);
        List<PinVO> records = pins.stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(records, (long) records.size(), page, size);
    }

    @Override
    public PageResult<PinVO> getTopicPins(Long topicId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Pin> pins = pinMapper.selectByTopicId(topicId, offset, size);
        List<PinVO> records = pins.stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResult.of(records, (long) records.size(), page, size);
    }

    @Override
    public List<PinTopicVO> getHotTopics(int limit) {
        List<PinTopic> topics = pinTopicMapper.selectAllEnabled();
        if (topics.size() > limit) {
            topics = topics.subList(0, limit);
        }
        return topics.stream().map(t -> {
            PinTopicVO vo = new PinTopicVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 沸点实体转VO，含用户信息
     */
    private PinVO convertToVO(Pin pin) {
        PinVO vo = new PinVO();
        BeanUtils.copyProperties(pin, vo);

        // 填充话题名称
        if (pin.getTopicId() != null) {
            PinTopic topic = pinTopicMapper.selectById(pin.getTopicId());
            if (topic != null) {
                vo.setTopicName(topic.getName());
            }
        }

        // 填充用户昵称和头像
        try {
            Result<UserInfoVO> result = userFeignClient.getUserById(pin.getUserId());
            if (result != null && result.getData() != null) {
                vo.setUserNickname(result.getData().getNickname());
                vo.setUserAvatar(result.getData().getAvatar());
            }
        } catch (Exception e) {
            log.debug("Failed to fetch pin author info: {}", e.getMessage());
        }

        return vo;
    }

}
