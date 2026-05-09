package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_social_link")
public class UserSocialLink extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
    /** 社交平台类型：github/blog/weibo/twitter */
    private String linkType;
    /** 社交链接地址 */
    private String linkUrl;

}
