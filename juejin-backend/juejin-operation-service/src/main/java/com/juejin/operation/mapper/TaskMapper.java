package com.juejin.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.operation.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务Mapper
 *
 * @author juejin
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 查询所有启用的任务
     */
    @Select("SELECT * FROM task WHERE status = 1 AND deleted = 0 ORDER BY type, sort_order")
    List<Task> selectAllEnabled();

    /**
     * 按类型查询任务
     */
    @Select("SELECT * FROM task WHERE type = #{type} AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<Task> selectByType(@Param("type") String type);

}
