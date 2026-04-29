package com.juejin.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务定义实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 任务名称 */
    private String name;

    /** 任务描述 */
    private String description;

    /** 任务类型：newbie-新手 daily-日常 */
    private String type;

    /** 任务代码（唯一标识） */
    private String taskCode;

    /** 完成条件值 */
    private Integer conditionValue;

    /** 掘力值奖励 */
    private Integer pointsReward;

    /** 徽章奖励ID */
    private Long badgeId;

    /** 排序值 */
    private Integer sortOrder;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

}
