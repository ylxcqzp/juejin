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

    /** 徽章名称 */
    private String name;
    /** 徽章描述 */
    private String description;
    /** 徽章图标URL */
    private String icon;
    /** 徽章类型 */
    private String type;
    /** 达成条件值 */
    private Integer conditionValue;
    /** 奖励掘力值 */
    private Integer points;
    /** 排序号 */
    private Integer sortOrder;
    /** 状态：0-禁用 1-启用 */
    private Integer status;

}
