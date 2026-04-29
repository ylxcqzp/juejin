package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.PinTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 沸点话题Mapper
 *
 * @author juejin
 */
@Mapper
public interface PinTopicMapper extends BaseMapper<PinTopic> {

    @Select("SELECT * FROM pin_topic WHERE status = 1 AND deleted = 0 ORDER BY pin_count DESC")
    List<PinTopic> selectAllEnabled();

    @Update("UPDATE pin_topic SET pin_count = pin_count + 1 WHERE id = #{id}")
    void incrementPinCount(@Param("id") Long id);

}
