package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 关注状态VO
 *
 * @author juejin
 */
@Data
@Schema(description = "用户间关注状态")
public class FollowStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "我是否关注了对方", example = "true")
    private Boolean isFollowing;

    @Schema(description = "对方是否关注了我", example = "false")
    private Boolean isFollowedBy;

    @Schema(description = "是否互相关注", example = "false")
    private Boolean isMutualFollow;

}
