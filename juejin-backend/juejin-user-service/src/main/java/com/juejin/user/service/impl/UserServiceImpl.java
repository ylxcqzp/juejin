package com.juejin.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.constants.RedisKey;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.starter.security.utils.JwtUtils;
import com.juejin.user.dto.*;
import com.juejin.user.entity.*;
import com.juejin.user.mapper.*;
import com.juejin.user.service.UserService;
import com.juejin.user.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserOauthMapper userOauthMapper;
    private final UserSocialLinkMapper userSocialLinkMapper;
    private final UserTagRelationMapper userTagRelationMapper;
    private final UserPrivacyMapper userPrivacyMapper;
    private final UserBadgeMapper userBadgeMapper;
    private final BadgeMapper badgeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String VERIFY_CODE_PREFIX = "verify:code:";
    private static final String VERIFY_LIMIT_PREFIX = "verify:limit:";
    private static final String TOKEN_BLACKLIST_PREFIX = "user:token:blacklist:";
    private static final String RESET_CODE_PREFIX = "password:reset:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        if (lambdaQuery().eq(User::getEmail, dto.getEmail()).exists()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setLevel(1);
        user.setPoints(0);
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setArticleCount(0);
        user.setLikeCount(0);
        user.setStatus(0);
        user.setRole(0);
        save(user);
        log.info("User registered: {}", user.getId());
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO registerByPhone(PhoneRegisterDTO dto) {
        if (lambdaQuery().eq(User::getPhone, dto.getPhone()).exists()) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        verifyCode(dto.getPhone(), dto.getCode(), "phone");
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setLevel(1);
        user.setPoints(0);
        user.setFollowingCount(0);
        user.setFollowerCount(0);
        user.setArticleCount(0);
        user.setLikeCount(0);
        user.setStatus(0);
        user.setRole(0);
        save(user);
        // 删除已使用的验证码
        redisTemplate.delete(VERIFY_CODE_PREFIX + "phone:" + dto.getPhone());
        log.info("User registered by phone: {}", user.getId());
        return convertToVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user;
        if ("phone".equals(dto.getLoginType())) {
            if (StrUtil.isBlank(dto.getPhone()) || StrUtil.isBlank(dto.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }
            user = userMapper.selectByPhone(dto.getPhone());
        } else {
            if (StrUtil.isBlank(dto.getEmail()) || StrUtil.isBlank(dto.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }
            user = userMapper.selectByEmail(dto.getEmail());
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 检查账号是否被锁定
        if (user.getLockUntilTime() != null && user.getLockUntilTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
            failCount++;
            user.setLoginFailCount(failCount);
            if (failCount >= 5) {
                user.setLockUntilTime(LocalDateTime.now().plusMinutes(15));
                log.warn("Account locked due to 5 failed login attempts: {}", user.getId());
            }
            updateById(user);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 登录成功，重置失败计数，更新登录信息
        user.setLoginFailCount(0);
        user.setLockUntilTime(null);
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        String accessToken = JwtUtils.generateToken(user.getId());
        String refreshToken = JwtUtils.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(
                RedisKey.USER_INFO + user.getId(),
                convertToVO(user),
                30,
                TimeUnit.MINUTES
        );

        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(1800L);
        loginVO.setUserInfo(convertToVO(user));
        return loginVO;
    }

    @Override
    public UserVO getUserById(Long userId) {
        UserVO cached = (UserVO) redisTemplate.opsForValue().get(RedisKey.USER_INFO + userId);
        if (cached != null) {
            return cached;
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserVO vo = convertToVO(user);
        redisTemplate.opsForValue().set(RedisKey.USER_INFO + userId, vo, 30, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return buildProfile(user, userId);
    }

    @Override
    public UserProfileVO getPublicProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        vo.setSocialLinks(getSocialLinks(userId));
        // 公开资料不包含手机号、隐私设置
        vo.setPhone(null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (StrUtil.isNotBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getBackgroundImage() != null) {
            user.setBackgroundImage(dto.getBackgroundImage());
        }
        updateById(user);
        redisTemplate.delete(RedisKey.USER_INFO + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserTags(Long userId, List<Long> tagIds) {
        if (tagIds != null && tagIds.size() > 5) {
            throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }
        // 删除旧的标签关联
        LambdaQueryWrapper<UserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTagRelation::getUserId, userId);
        userTagRelationMapper.delete(wrapper);
        // 插入新的标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                UserTagRelation relation = new UserTagRelation();
                relation.setUserId(userId);
                relation.setTagId(tagId);
                userTagRelationMapper.insert(relation);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSocialLinks(Long userId, List<UpdateSocialLinksDTO.SocialLinkItem> links) {
        LambdaQueryWrapper<UserSocialLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSocialLink::getUserId, userId);
        userSocialLinkMapper.delete(wrapper);
        if (links != null && !links.isEmpty()) {
            for (UpdateSocialLinksDTO.SocialLinkItem item : links) {
                UserSocialLink link = new UserSocialLink();
                link.setUserId(userId);
                link.setLinkType(item.getLinkType());
                link.setLinkUrl(item.getLinkUrl());
                userSocialLinkMapper.insert(link);
            }
        }
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 检查 refreshToken 是否在黑名单中
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + refreshToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!JwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = JwtUtils.getUserIdFromToken(refreshToken);
        User user = getById(userId);
        if (user == null || user.getStatus() != 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 将旧 refreshToken 加入黑名单（一次性使用）
        redisTemplate.opsForValue().set(blacklistKey, "1", 7, TimeUnit.DAYS);
        // 生成新 Token
        String newAccessToken = JwtUtils.generateToken(userId);
        String newRefreshToken = JwtUtils.generateRefreshToken(userId);

        redisTemplate.opsForValue().set(RedisKey.USER_INFO + userId, convertToVO(user), 30, TimeUnit.MINUTES);

        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(userId);
        loginVO.setAccessToken(newAccessToken);
        loginVO.setRefreshToken(newRefreshToken);
        loginVO.setExpiresIn(1800L);
        loginVO.setUserInfo(convertToVO(user));
        return loginVO;
    }

    @Override
    public void sendVerifyCode(VerifyCodeDTO dto) {
        checkVerifyRateLimit(dto.getAccount(), dto.getType());
        // 生成6位随机验证码，存入 Redis，5分钟有效
        String code = generateVerificationCode();
        String key = VERIFY_CODE_PREFIX + dto.getType() + ":" + dto.getAccount();
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("Verification code sent to {}:{} -> {}", dto.getType(), dto.getAccount(), code);
    }

    @Override
    public void sendPasswordResetCode(PasswordResetDTO dto) {
        // 检查账号是否存在
        boolean exists;
        if ("phone".equals(dto.getType())) {
            exists = lambdaQuery().eq(User::getPhone, dto.getAccount()).exists();
        } else {
            exists = lambdaQuery().eq(User::getEmail, dto.getAccount()).exists();
        }
        if (!exists) {
            // 不泄露用户是否存在，直接返回成功
            log.info("Password reset code requested for non-existent account: {}", dto.getAccount());
            return;
        }
        checkVerifyRateLimit(dto.getAccount(), dto.getType());
        // 生成6位随机验证码，存入 Redis，5分钟有效
        String code = generateVerificationCode();
        String key = RESET_CODE_PREFIX + dto.getType() + ":" + dto.getAccount();
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("Password reset code sent to {}:{} -> {}", dto.getType(), dto.getAccount(), code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(PasswordResetConfirmDTO dto) {
        String key = RESET_CODE_PREFIX + dto.getType() + ":" + dto.getAccount();
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!storedCode.equals(dto.getCode())) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
        }
        User user;
        if ("phone".equals(dto.getType())) {
            user = userMapper.selectByPhone(dto.getAccount());
        } else {
            user = userMapper.selectByEmail(dto.getAccount());
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
        redisTemplate.delete(key);
        log.info("Password reset for user: {}", user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, BindAccountDTO dto) {
        verifyCode(dto.getAccount(), dto.getCode(), "phone");
        if (lambdaQuery().eq(User::getPhone, dto.getAccount()).exists()) {
            throw new BusinessException(ErrorCode.ALREADY_BOUND);
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPhone(dto.getAccount());
        updateById(user);
        redisTemplate.delete(VERIFY_CODE_PREFIX + "phone:" + dto.getAccount());
        redisTemplate.delete(RedisKey.USER_INFO + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindEmail(Long userId, BindAccountDTO dto) {
        verifyCode(dto.getAccount(), dto.getCode(), "email");
        if (lambdaQuery().eq(User::getEmail, dto.getAccount()).exists()) {
            throw new BusinessException(ErrorCode.ALREADY_BOUND);
        }
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setEmail(dto.getAccount());
        updateById(user);
        redisTemplate.delete(VERIFY_CODE_PREFIX + "email:" + dto.getAccount());
        redisTemplate.delete(RedisKey.USER_INFO + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindAccount(Long userId, String type) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if ("phone".equals(type)) {
            if (StrUtil.isBlank(user.getPhone())) {
                throw new BusinessException(ErrorCode.PHONE_NOT_BOUND);
            }
            user.setPhone(null);
        } else if ("email".equals(type)) {
            if (StrUtil.isBlank(user.getEmail())) {
                throw new BusinessException(ErrorCode.EMAIL_NOT_BOUND);
            }
            user.setEmail(null);
        } else {
            throw new BusinessException(ErrorCode.NO_BINDING);
        }
        updateById(user);
        redisTemplate.delete(RedisKey.USER_INFO + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyAccountCancellation(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.ACCOUNT_CANCELLATION_APPLIED);
        }
        user.setStatus(2);
        user.setCancelApplyTime(LocalDateTime.now());
        updateById(user);
        redisTemplate.delete(RedisKey.USER_INFO + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeAccountCancellation(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 2) {
            throw new BusinessException(ErrorCode.CANCELLATION_NOT_APPLIED);
        }
        user.setStatus(0);
        user.setCancelApplyTime(null);
        updateById(user);
        redisTemplate.delete(RedisKey.USER_INFO + userId);
    }

    @Override
    public PointsVO getPoints(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        int points = user.getPoints() == null ? 0 : user.getPoints();
        int level = calculateLevel(points);
        int[] levelThresholds = {0, 100, 500, 2000, 5000, 10000};
        String[] levelTitles = {"萌新", "初学者", "进阶者", "熟练者", "专家", "大神"};
        int currentLevelIdx = Math.min(level - 1, 5);
        PointsVO vo = new PointsVO();
        vo.setCurrentPoints(points);
        vo.setLevel(level);
        vo.setLevelTitle(levelTitles[currentLevelIdx]);
        if (level < 6) {
            vo.setNextLevelPoints(levelThresholds[level]);
            vo.setPointsToNextLevel(levelThresholds[level] - points);
        } else {
            vo.setNextLevelPoints(null);
            vo.setPointsToNextLevel(0);
        }
        return vo;
    }

    @Override
    public List<UserBadgeVO> getUserBadges(Long userId) {
        LambdaQueryWrapper<UserBadge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBadge::getUserId, userId);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addPoints(Long userId, Integer points) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPoints(user.getPoints() + points);
        userMapper.updateById(user);
        // 清除缓存
        redisTemplate.delete(RedisKey.USER_INFO + userId);
        log.info("Points added: userId={}, points={}, total={}", userId, points, user.getPoints());
        return user.getPoints();
    }

    // --- private helpers ---

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private UserProfileVO buildProfile(User user, Long userId) {
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        vo.setSocialLinks(getSocialLinks(userId));
        vo.setTags(Collections.emptyList()); // 标签依赖 content-service 的 tag 表，暂时返回空
        vo.setPrivacy(getPrivacyVO(userId));
        vo.setBadges(getUserBadges(userId));
        return vo;
    }

    private List<UserProfileVO.SocialLinkVO> getSocialLinks(Long userId) {
        LambdaQueryWrapper<UserSocialLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSocialLink::getUserId, userId);
        List<UserSocialLink> links = userSocialLinkMapper.selectList(wrapper);
        if (links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream().map(this::toSocialLinkVO).collect(Collectors.toList());
    }

    private UserProfileVO.SocialLinkVO toSocialLinkVO(UserSocialLink link) {
        UserProfileVO.SocialLinkVO sl = new UserProfileVO.SocialLinkVO();
        sl.setLinkType(link.getLinkType());
        sl.setLinkUrl(link.getLinkUrl());
        return sl;
    }

    private UserProfileVO.PrivacyVO getPrivacyVO(Long userId) {
        LambdaQueryWrapper<UserPrivacy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPrivacy::getUserId, userId);
        UserPrivacy privacy = userPrivacyMapper.selectOne(wrapper);
        UserProfileVO.PrivacyVO pvo = new UserProfileVO.PrivacyVO();
        if (privacy != null) {
            pvo.setShowFavorites(privacy.getShowFavorites());
            pvo.setShowFollowing(privacy.getShowFollowing());
            pvo.setShowFollowers(privacy.getShowFollowers());
            pvo.setAllowStrangerMessage(privacy.getAllowStrangerMessage());
            pvo.setMessagePushEnabled(privacy.getMessagePushEnabled());
        }
        return pvo;
    }

    private void verifyCode(String account, String code, String type) {
        String key = VERIFY_CODE_PREFIX + type + ":" + account;
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
        }
    }

    private void checkVerifyRateLimit(String account, String type) {
        String limitKey = VERIFY_LIMIT_PREFIX + type + ":" + account;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_TOO_FREQUENT);
        }
        redisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);
    }

    /**
     * 生成6位随机数字验证码（开发阶段通过日志打印，后续可对接短信/邮件服务）
     */
    private String generateVerificationCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    private int calculateLevel(int points) {
        if (points >= 10000) return 6;
        if (points >= 5000) return 5;
        if (points >= 2000) return 4;
        if (points >= 500) return 3;
        if (points >= 100) return 2;
        return 1;
    }
}
