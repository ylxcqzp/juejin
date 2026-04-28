package com.juejin.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PointsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer currentPoints;
    private Integer level;
    private String levelTitle;
    private Integer nextLevelPoints;
    private Integer pointsToNextLevel;

}
