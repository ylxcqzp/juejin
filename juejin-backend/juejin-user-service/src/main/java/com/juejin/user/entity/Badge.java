package com.juejin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("badge")
public class Badge extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String icon;
    private String type;
    private Integer conditionValue;
    private Integer points;
    private Integer sortOrder;
    private Integer status;

}
