package com.juejin.operation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 签到结果VO
 *
 * @author juejin
 */
@Data
@Schema(description = "签到结果")
public class SignVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否签到成功", example = "true")
    private Boolean signed;

    @Schema(description = "签到日期", example = "2024-03-21")
    private LocalDate signDate;

    @Schema(description = "连续签到天数", example = "15")
    private Integer continuousDays;

    @Schema(description = "本次签到获得掘力值", example = "1")
    private Integer pointsEarned;

    @Schema(description = "连续签到奖励描述", example = "连续签到7天，额外+5")
    private String bonusDesc;

    @Schema(description = "当月签到日期列表（日历展示用）", example = "[\"2024-03-01\",\"2024-03-02\"]")
    private List<LocalDate> signedDates;

    @Schema(description = "当前总掘力值", example = "535")
    private Integer totalPoints;

}
