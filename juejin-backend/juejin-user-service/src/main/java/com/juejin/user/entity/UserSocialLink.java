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

    private Long userId;
    private String linkType;
    private String linkUrl;

}
