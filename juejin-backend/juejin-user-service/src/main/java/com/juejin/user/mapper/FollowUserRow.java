package com.juejin.user.mapper;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserRow {

    private Long id;
    private Long userId;
    private Long followingId;
    private Integer status;
    private LocalDateTime createTime;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer level;

}
