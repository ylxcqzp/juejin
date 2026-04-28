package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private String bio;
    private String backgroundImage;
    private Integer level;
    private Integer points;
    private Integer followingCount;
    private Integer followerCount;
    private Integer articleCount;
    private Integer likeCount;
    private Integer role;
    private LocalDateTime createTime;

}
