package com.juejin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.content.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章标签关联Mapper
 *
 * @author juejin
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /**
     * 根据文章ID查询关联的标签ID列表
     *
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId} AND deleted = 0")
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);

}
