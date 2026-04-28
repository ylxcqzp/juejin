package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BadgeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String type;
    private Integer conditionValue;
    private Integer points;

}
