package com.juejin.operation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务VO
 *
 * @author juejin
 */
@Data
@Schema(description = "任务信息（含用户进度）")
public class TaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "任务名称", example = "完善个人资料")
    private String name;

    @Schema(description = "任务描述", example = "完善个人资料信息可获得掘力值奖励")
    private String description;

    @Schema(description = "任务类型：newbie-新手 daily-日常", example = "newbie")
    private String type;

    @Schema(description = "任务代码（唯一标识）", example = "complete_profile")
    private String taskCode;

    @Schema(description = "完成条件值", example = "1")
    private Integer conditionValue;

    @Schema(description = "掘力值奖励", example = "5")
    private Integer pointsReward;

    @Schema(description = "徽章奖励ID")
    private Long badgeId;

    @Schema(description = "排序值", example = "0")
    private Integer sortOrder;

    @Schema(description = "当前用户进度", example = "1")
    private Integer progress;

    @Schema(description = "是否已完成", example = "true")
    private Boolean isCompleted;

    @Schema(description = "是否已领取奖励", example = "false")
    private Boolean isClaimed;

}
