package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FollowUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer level;
    private Boolean isMutualFollow;
    private LocalDateTime followTime;

}
