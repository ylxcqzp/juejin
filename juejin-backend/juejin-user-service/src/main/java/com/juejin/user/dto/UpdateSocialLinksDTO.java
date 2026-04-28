package com.juejin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "更新社交链接请求")
public class UpdateSocialLinksDTO {

    @Valid
    @Size(max = 10, message = "社交链接最多10条")
    @Schema(description = "社交链接列表")
    private List<SocialLinkItem> links;

    @Data
    @Schema(description = "社交链接项")
    public static class SocialLinkItem {

        @NotBlank(message = "链接类型不能为空")
        @Schema(description = "链接类型：github/blog/weibo/zhihu等", example = "github")
        private String linkType;

        @NotBlank(message = "链接地址不能为空")
        @Schema(description = "链接地址", example = "https://github.com/user")
        private String linkUrl;

    }

}
