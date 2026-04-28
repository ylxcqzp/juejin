package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 标签Mapper接口
 *
 * @author juejin
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 增加文章数量
     */
    @Update("UPDATE tag SET article_count = article_count + 1 WHERE id = #{id}")
    void incrementArticleCount(@Param("id") Long id);

    /**
     * 减少文章数量
     */
    @Update("UPDATE tag SET article_count = article_count - 1 WHERE id = #{id} AND article_count > 0")
    void decrementArticleCount(@Param("id") Long id);

    /**
     * 增加关注数量
     */
    @Update("UPDATE tag SET follow_count = follow_count + 1 WHERE id = #{id}")
    void incrementFollowCount(@Param("id") Long id);

    /**
     * 减少关注数量
     */
    @Update("UPDATE tag SET follow_count = follow_count - 1 WHERE id = #{id} AND follow_count > 0")
    void decrementFollowCount(@Param("id") Long id);

}
