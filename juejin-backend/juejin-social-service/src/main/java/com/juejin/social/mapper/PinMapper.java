package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.Pin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 沸点Mapper
 *
 * @author juejin
 */
@Mapper
public interface PinMapper extends BaseMapper<Pin> {

    /**
     * 查询关注用户的沸点列表
     */
    @Select("SELECT p.* FROM pin p WHERE p.user_id IN " +
            "(SELECT uf.following_id FROM user_follow uf WHERE uf.user_id = #{userId} AND uf.status = 1) " +
            "AND p.status = 1 AND p.deleted = 0 ORDER BY p.create_time DESC LIMIT #{offset}, #{limit}")
    List<Pin> selectFollowingPins(@Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 查询热门沸点
     */
    @Select("SELECT * FROM pin WHERE status = 1 AND deleted = 0 " +
            "ORDER BY like_count DESC, create_time DESC LIMIT #{limit}")
    List<Pin> selectHotPins(@Param("limit") int limit);

    /**
     * 查询话题下的沸点
     */
    @Select("SELECT * FROM pin WHERE topic_id = #{topicId} AND status = 1 AND deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Pin> selectByTopicId(@Param("topicId") Long topicId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

}
