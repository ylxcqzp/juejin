package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 掘力值与等级VO
 *
 * @author juejin
 */
@Data
@Schema(description = "掘力值与等级信息")
public class PointsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前掘力值", example = "520")
    private Integer currentPoints;

    @Schema(description = "当前等级：1-萌新 2-初学者 3-进阶者 4-熟练者 5-专家 6-大神", example = "3")
    private Integer level;

    @Schema(description = "等级称号", example = "进阶者")
    private String levelTitle;

    @Schema(description = "下一级所需掘力值", example = "2000")
    private Integer nextLevelPoints;

    @Schema(description = "距离下一级还差多少掘力值", example = "1480")
    private Integer pointsToNextLevel;

}
