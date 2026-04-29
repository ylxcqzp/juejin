package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.ArticleVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章版本Mapper
 *
 * @author juejin
 */
@Mapper
public interface ArticleVersionMapper extends BaseMapper<ArticleVersion> {

    /**
     * 根据文章ID查询所有版本，按版本号降序
     */
    @Select("SELECT * FROM article_version WHERE article_id = #{articleId} AND deleted = 0 " +
            "ORDER BY version DESC")
    List<ArticleVersion> selectByArticleId(@Param("articleId") Long articleId);

    /**
     * 查询文章的最大版本号
     */
    @Select("SELECT COALESCE(MAX(version), 0) FROM article_version WHERE article_id = #{articleId}")
    Integer selectMaxVersion(@Param("articleId") Long articleId);

}
