package com.juejin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.user.dto.*;
import com.juejin.user.entity.User;
import com.juejin.user.vo.*;

public interface UserService extends IService<User> {

    UserVO register(RegisterDTO dto);

    UserVO registerByPhone(PhoneRegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getUserById(Long userId);

    UserProfileVO getProfile(Long userId);

    UserProfileVO getPublicProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);

    void updateUserTags(Long userId, java.util.List<Long> tagIds);

    void updateSocialLinks(Long userId, java.util.List<UpdateSocialLinksDTO.SocialLinkItem> links);

    LoginVO refreshToken(String refreshToken);

    void sendVerifyCode(VerifyCodeDTO dto);

    void sendPasswordResetCode(PasswordResetDTO dto);

    void resetPassword(PasswordResetConfirmDTO dto);

    void bindPhone(Long userId, BindAccountDTO dto);

    void bindEmail(Long userId, BindAccountDTO dto);

    void unbindAccount(Long userId, String type);

    void applyAccountCancellation(Long userId);

    void revokeAccountCancellation(Long userId);

    PointsVO getPoints(Long userId);

    java.util.List<UserBadgeVO> getUserBadges(Long userId);

    /**
     * 增加用户掘力值（内部调用：签到/任务奖励）
     *
     * @param userId 用户ID
     * @param points 要增加的掘力值
     * @return 增加后的掘力值
     */
    Integer addPoints(Long userId, Integer points);

}
