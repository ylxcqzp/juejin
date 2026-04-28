package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新隐私设置请求")
public class UpdatePrivacyDTO {

    @Schema(description = "是否公开收藏夹：0-否 1-是", example = "1")
    private Integer showFavorites;

    @Schema(description = "是否公开关注列表：0-否 1-是", example = "1")
    private Integer showFollowing;

    @Schema(description = "是否公开粉丝列表：0-否 1-是", example = "1")
    private Integer showFollowers;

    @Schema(description = "允许陌生人私信：0-否 1-是 2-仅互关", example = "1")
    private Integer allowStrangerMessage;

    @Schema(description = "消息推送开关：0-否 1-是", example = "1")
    private Integer messagePushEnabled;

}
