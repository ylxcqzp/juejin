package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.Draft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 草稿Mapper
 *
 * @author juejin
 */
@Mapper
public interface DraftMapper extends BaseMapper<Draft> {

    /**
     * 统计用户的草稿数量
     */
    @Select("SELECT COUNT(*) FROM draft WHERE user_id = #{userId} AND deleted = 0")
    long countByUserId(@Param("userId") Long userId);

}
