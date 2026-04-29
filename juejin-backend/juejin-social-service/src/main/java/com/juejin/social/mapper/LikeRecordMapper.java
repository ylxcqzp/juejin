package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点赞记录Mapper
 *
 * @author juejin
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    /**
     * 查询最近点赞的用户ID列表
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param limit      数量限制
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM like_record WHERE target_id = #{targetId} " +
            "AND target_type = #{targetType} AND status = 1 AND deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<Long> selectRecentLikeUserIds(@Param("targetId") Long targetId,
                                        @Param("targetType") Integer targetType,
                                        @Param("limit") int limit);

    /**
     * 统计目标点赞数
     */
    @Select("SELECT COUNT(*) FROM like_record WHERE target_id = #{targetId} " +
            "AND target_type = #{targetType} AND status = 1 AND deleted = 0")
    long countLikes(@Param("targetId") Long targetId,
                    @Param("targetType") Integer targetType);

}
