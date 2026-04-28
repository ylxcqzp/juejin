package com.juejin.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juejin.common.exception.BusinessException;
import com.juejin.common.result.ErrorCode;
import com.juejin.common.utils.JwtUtils;
import com.juejin.user.dto.LoginDTO;
import com.juejin.user.dto.RegisterDTO;
import com.juejin.user.entity.User;
import com.juejin.user.mapper.UserMapper;
import com.juejin.user.service.UserService;
import com.juejin.user.vo.LoginVO;
import com.juejin.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        // 检查邮箱是否已注册
        if (lambdaQuery().eq(User::getEmail, dto.getEmail()).exists()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 创建用户
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
        user.setRole(0); // 默认普通用户

        save(user);
        log.info("User registered: {}", user.getId());

        return convertToVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectByEmail(dto.getEmail());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 生成Token
        String accessToken = JwtUtils.generateToken(user.getId());
        String refreshToken = JwtUtils.generateRefreshToken(user.getId());

        // 缓存用户信息
        redisTemplate.opsForValue().set(
                "user:" + user.getId(),
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
        // 先从缓存获取
        UserVO cached = (UserVO) redisTemplate.opsForValue().get("user:" + userId);
        if (cached != null) {
            return cached;
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserVO vo = convertToVO(user);
        
        // 缓存用户信息
        redisTemplate.opsForValue().set(
                "user:" + userId,
                vo,
                30,
                TimeUnit.MINUTES
        );

        return vo;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

}
