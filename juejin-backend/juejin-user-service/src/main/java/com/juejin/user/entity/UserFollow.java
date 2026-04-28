package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_follow")
public class UserFollow extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long followingId;
    private Integer status;

    @Version
    private Integer version;

}
