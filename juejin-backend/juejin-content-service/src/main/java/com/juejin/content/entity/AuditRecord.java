package com.juejin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.juejin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核记录实体类
 *
 * @author juejin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_record")
public class AuditRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 目标ID（文章/评论/沸点） */
    private Long targetId;

    /** 目标类型：article/comment/pin */
    private String targetType;

    /** 审核人ID（机器审核为空） */
    private Long auditorId;

    /** 审核类型：1-机器 2-人工 */
    private Integer auditType;

    /** 审核结果：1-通过 2-驳回 3-删除 */
    private Integer auditResult;

    /** 审核原因 */
    private String auditReason;

    /** 命中的敏感词 */
    private String sensitiveWords;

    /** 风险等级：1-低 2-中 3-高 */
    private Integer riskLevel;

}
