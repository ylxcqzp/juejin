package com.juejin.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.operation.entity.SignRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到记录Mapper
 *
 * @author juejin
 */
@Mapper
public interface SignRecordMapper extends BaseMapper<SignRecord> {

    /**
     * 查询用户指定日期的签到记录
     */
    @Select("SELECT * FROM sign_record WHERE user_id = #{userId} AND sign_date = #{date} AND deleted = 0")
    SignRecord selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户近期的签到记录（用于计算连续签到天数）
     */
    @Select("SELECT * FROM sign_record WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY sign_date DESC LIMIT #{limit}")
    List<SignRecord> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

}
