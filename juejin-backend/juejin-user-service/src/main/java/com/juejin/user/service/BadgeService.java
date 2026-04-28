package com.juejin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.user.entity.Badge;
import com.juejin.user.vo.BadgeVO;
import com.juejin.user.vo.UserBadgeVO;

import java.util.List;

public interface BadgeService extends IService<Badge> {

    List<BadgeVO> getAllBadges();

    List<UserBadgeVO> getUserBadges(Long userId);

}
