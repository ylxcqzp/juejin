package com.juejin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.user.dto.LoginDTO;
import com.juejin.user.dto.RegisterDTO;
import com.juejin.user.entity.User;
import com.juejin.user.vo.LoginVO;
import com.juejin.user.vo.UserVO;

public interface UserService extends IService<User> {

    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getUserById(Long userId);

}
