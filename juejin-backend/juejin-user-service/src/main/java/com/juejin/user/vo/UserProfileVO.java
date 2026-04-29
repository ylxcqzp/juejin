package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户完整个人资料VO（含社交链接、标签、隐私设置、徽章）
 *
 * @author juejin
 */
@Data
@Schema(description = "用户完整个人资料")
public class UserProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://cdn.example.com/avatar/1.png")
    private String avatar;

    @Schema(description = "个人简介", example = "一名热爱技术的全栈开发者")
    private String bio;

    @Schema(description = "个人主页背景图URL")
    private String backgroundImage;

    @Schema(description = "用户等级", example = "3")
    private Integer level;

    @Schema(description = "掘力值", example = "520")
    private Integer points;

    @Schema(description = "关注数", example = "42")
    private Integer followingCount;

    @Schema(description = "粉丝数", example = "128")
    private Integer followerCount;

    @Schema(description = "文章数", example = "15")
    private Integer articleCount;

    @Schema(description = "获赞数", example = "256")
    private Integer likeCount;

    @Schema(description = "角色：0-普通用户 1-认证作者 2-管理员", example = "0")
    private Integer role;

    @Schema(description = "注册时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "社交链接列表")
    private List<SocialLinkVO> socialLinks;

    @Schema(description = "技能标签列表")
    private List<TagBriefVO> tags;

    @Schema(description = "隐私设置")
    private PrivacyVO privacy;

    @Schema(description = "已获得徽章列表")
    private List<UserBadgeVO> badges;

    @Data
    @Schema(description = "社交链接")
    public static class SocialLinkVO {
        @Schema(description = "链接类型：github/blog/weibo/zhihu", example = "github")
        private String linkType;

        @Schema(description = "链接地址", example = "https://github.com/zhangsan")
        private String linkUrl;
    }

    @Data
    @Schema(description = "技能标签简要信息")
    public static class TagBriefVO {
        @Schema(description = "标签ID", example = "1")
        private Long tagId;

        @Schema(description = "标签名称", example = "Java")
        private String tagName;
    }

    @Data
    @Schema(description = "隐私设置")
    public static class PrivacyVO {
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

}
