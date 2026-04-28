package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 分类Mapper接口
 *
 * @author juejin
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 增加文章数量
     */
    @Update("UPDATE category SET article_count = article_count + 1 WHERE id = #{id}")
    void incrementArticleCount(@Param("id") Long id);

    /**
     * 减少文章数量
     */
    @Update("UPDATE category SET article_count = article_count - 1 WHERE id = #{id} AND article_count > 0")
    void decrementArticleCount(@Param("id") Long id);

}
