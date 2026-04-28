package com.juejin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.user.dto.UpdatePrivacyDTO;
import com.juejin.user.entity.UserPrivacy;
import com.juejin.user.mapper.UserMapper;
import com.juejin.user.mapper.UserPrivacyMapper;
import com.juejin.user.service.PrivacyService;
import com.juejin.user.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrivacyServiceImpl extends ServiceImpl<UserPrivacyMapper, UserPrivacy> implements PrivacyService {

    private final UserPrivacyMapper userPrivacyMapper;
    private final UserMapper userMapper;

    @Override
    public UserProfileVO.PrivacyVO getPrivacy(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        LambdaQueryWrapper<UserPrivacy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPrivacy::getUserId, userId);
        UserPrivacy privacy = userPrivacyMapper.selectOne(wrapper);
        UserProfileVO.PrivacyVO vo = new UserProfileVO.PrivacyVO();
        if (privacy != null) {
            vo.setShowFavorites(privacy.getShowFavorites());
            vo.setShowFollowing(privacy.getShowFollowing());
            vo.setShowFollowers(privacy.getShowFollowers());
            vo.setAllowStrangerMessage(privacy.getAllowStrangerMessage());
            vo.setMessagePushEnabled(privacy.getMessagePushEnabled());
        } else {
            vo.setShowFavorites(1);
            vo.setShowFollowing(1);
            vo.setShowFollowers(1);
            vo.setAllowStrangerMessage(1);
            vo.setMessagePushEnabled(1);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacy(Long userId, UpdatePrivacyDTO dto) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        LambdaQueryWrapper<UserPrivacy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPrivacy::getUserId, userId);
        UserPrivacy privacy = userPrivacyMapper.selectOne(wrapper);
        if (privacy == null) {
            privacy = new UserPrivacy();
            privacy.setUserId(userId);
            privacy.setShowFavorites(1);
            privacy.setShowFollowing(1);
            privacy.setShowFollowers(1);
            privacy.setAllowStrangerMessage(1);
            privacy.setMessagePushEnabled(1);
        }
        if (dto.getShowFavorites() != null) {
            privacy.setShowFavorites(dto.getShowFavorites());
        }
        if (dto.getShowFollowing() != null) {
            privacy.setShowFollowing(dto.getShowFollowing());
        }
        if (dto.getShowFollowers() != null) {
            privacy.setShowFollowers(dto.getShowFollowers());
        }
        if (dto.getAllowStrangerMessage() != null) {
            privacy.setAllowStrangerMessage(dto.getAllowStrangerMessage());
        }
        if (dto.getMessagePushEnabled() != null) {
            privacy.setMessagePushEnabled(dto.getMessagePushEnabled());
        }
        if (privacy.getId() == null) {
            userPrivacyMapper.insert(privacy);
        } else {
            userPrivacyMapper.updateById(privacy);
        }
    }
}
