package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_badge")
public class UserBadge extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long badgeId;
    private LocalDateTime obtainTime;

}
