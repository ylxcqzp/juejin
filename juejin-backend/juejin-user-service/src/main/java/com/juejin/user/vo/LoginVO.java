package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserVO userInfo;

}
