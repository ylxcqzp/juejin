package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileVO implements Serializable {

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
    private List<SocialLinkVO> socialLinks;
    private List<TagBriefVO> tags;
    private PrivacyVO privacy;
    private List<UserBadgeVO> badges;

    @Data
    public static class SocialLinkVO {
        private String linkType;
        private String linkUrl;
    }

    @Data
    public static class TagBriefVO {
        private Long tagId;
        private String tagName;
    }

    @Data
    public static class PrivacyVO {
        private Integer showFavorites;
        private Integer showFollowing;
        private Integer showFollowers;
        private Integer allowStrangerMessage;
        private Integer messagePushEnabled;
    }

}
