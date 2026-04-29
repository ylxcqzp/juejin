package com.juejin.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 沸点话题实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pin_topic")
public class PinTopic extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 话题名称 */
    private String name;

    /** 话题描述 */
    private String description;

    /** 封面图 */
    private String coverImage;

    /** 沸点数 */
    private Integer pinCount;

    /** 关注人数 */
    private Integer followCount;

    /** 是否热门 */
    private Integer isHot;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

}
