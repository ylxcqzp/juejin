package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Schema(description = "更新个人资料请求")
public class UpdateProfileDTO {

    @Size(min = 2, max = 20, message = "昵称长度为2-20位")
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Size(max = 200, message = "简介长度不能超过200")
    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "个人主页背景图URL")
    private String backgroundImage;

}
