package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 文章Mapper接口
 *
 * @author juejin
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 增加浏览量
     */
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    @Update("UPDATE article SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    /**
     * 减少点赞数
     */
    @Update("UPDATE article SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(@Param("id") Long id);

    /**
     * 增加评论数
     */
    @Update("UPDATE article SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(@Param("id") Long id);

    /**
     * 减少评论数
     */
    @Update("UPDATE article SET comment_count = comment_count - 1 WHERE id = #{id} AND comment_count > 0")
    void decrementCommentCount(@Param("id") Long id);

    /**
     * 查询热门文章
     */
    @Select("SELECT * FROM article WHERE status = 2 AND deleted = 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Article> selectHotArticles(@Param("limit") Integer limit);

}
