package com.juejin.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.operation.entity.UserTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户任务Mapper
 *
 * @author juejin
 */
@Mapper
public interface UserTaskMapper extends BaseMapper<UserTask> {

    /**
     * 查询用户指定日期的任务记录
     */
    @Select("SELECT * FROM user_task WHERE user_id = #{userId} AND task_date = #{date} " +
            "AND deleted = 0")
    List<UserTask> selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户指定任务的最新记录
     */
    @Select("SELECT * FROM user_task WHERE user_id = #{userId} AND task_id = #{taskId} " +
            "AND deleted = 0 ORDER BY task_date DESC LIMIT 1")
    UserTask selectByUserIdAndTaskId(@Param("userId") Long userId, @Param("taskId") Long taskId);

}
