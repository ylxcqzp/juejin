package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FollowStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isFollowing;
    private Boolean isFollowedBy;
    private Boolean isMutualFollow;

}
