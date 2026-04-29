package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper
 *
 * @author juejin
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {

}
