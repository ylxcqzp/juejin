package com.juejin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.user.entity.Badge;
import com.juejin.user.entity.UserBadge;
import com.juejin.user.mapper.BadgeMapper;
import com.juejin.user.mapper.UserBadgeMapper;
import com.juejin.user.service.BadgeService;
import com.juejin.user.vo.BadgeVO;
import com.juejin.user.vo.UserBadgeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl extends ServiceImpl<BadgeMapper, Badge> implements BadgeService {

    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;

    @Override
    public List<BadgeVO> getAllBadges() {
        LambdaQueryWrapper<Badge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Badge::getStatus, 1).orderByAsc(Badge::getSortOrder);
        List<Badge> badges = badgeMapper.selectList(wrapper);
        return badges.stream().map(b -> {
            BadgeVO vo = new BadgeVO();
            vo.setId(b.getId());
            vo.setName(b.getName());
            vo.setDescription(b.getDescription());
            vo.setIcon(b.getIcon());
            vo.setType(b.getType());
            vo.setConditionValue(b.getConditionValue());
            vo.setPoints(b.getPoints());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserBadgeVO> getUserBadges(Long userId) {
        LambdaQueryWrapper<UserBadge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBadge::getUserId, userId).orderByDesc(UserBadge::getObtainTime);
        List<UserBadge> userBadges = userBadgeMapper.selectList(wrapper);
        if (userBadges.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> badgeIds = userBadges.stream().map(UserBadge::getBadgeId).collect(Collectors.toList());
        List<Badge> badges = badgeMapper.selectBatchIds(badgeIds);
        return userBadges.stream().map(ub -> {
            Badge badge = badges.stream().filter(b -> b.getId().equals(ub.getBadgeId())).findFirst().orElse(null);
            UserBadgeVO vo = new UserBadgeVO();
            if (badge != null) {
                vo.setId(badge.getId());
                vo.setName(badge.getName());
                vo.setDescription(badge.getDescription());
                vo.setIcon(badge.getIcon());
                vo.setType(badge.getType());
                vo.setConditionValue(badge.getConditionValue());
                vo.setPoints(badge.getPoints());
            }
            vo.setObtainTime(ub.getObtainTime());
            return vo;
        }).collect(Collectors.toList());
    }
}
