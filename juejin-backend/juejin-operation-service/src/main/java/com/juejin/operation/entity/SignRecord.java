package com.juejin.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 签到记录实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sign_record")
public class SignRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 签到日期 */
    private LocalDate signDate;

    /** 连续签到天数 */
    private Integer continuousDays;

    /** 获得的掘力值 */
    private Integer pointsEarned;

}
