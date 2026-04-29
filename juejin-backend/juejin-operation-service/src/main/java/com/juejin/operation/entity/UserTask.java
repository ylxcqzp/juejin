package com.juejin.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户任务记录实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_task")
public class UserTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 任务ID */
    private Long taskId;

    /** 当前进度 */
    private Integer progress;

    /** 是否完成：0-否 1-是 */
    private Integer isCompleted;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 是否已领取奖励：0-否 1-是 */
    private Integer isClaimed;

    /** 领取时间 */
    private LocalDateTime claimTime;

    /** 任务日期（日常任务用） */
    private LocalDate taskDate;

}
