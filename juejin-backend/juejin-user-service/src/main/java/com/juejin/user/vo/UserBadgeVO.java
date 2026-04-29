package com.juejin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户徽章VO
 *
 * @author juejin
 */
@Data
@Schema(description = "用户获得的徽章")
public class UserBadgeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "徽章ID", example = "1")
    private Long id;

    @Schema(description = "徽章名称", example = "连续签到7天")
    private String name;

    @Schema(description = "徽章描述", example = "连续签到7天可获得此徽章")
    private String description;

    @Schema(description = "徽章图标URL", example = "/badges/sign_7.png")
    private String icon;

    @Schema(description = "徽章类型：register/sign/article/view/like", example = "sign")
    private String type;

    @Schema(description = "达成条件值", example = "7")
    private Integer conditionValue;

    @Schema(description = "奖励掘力值", example = "5")
    private Integer points;

    @Schema(description = "获得时间", example = "2024-03-15T08:00:00")
    private LocalDateTime obtainTime;

}
