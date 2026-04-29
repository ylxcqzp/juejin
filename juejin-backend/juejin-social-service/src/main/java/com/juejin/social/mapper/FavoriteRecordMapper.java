package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.FavoriteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏记录Mapper
 *
 * @author juejin
 */
@Mapper
public interface FavoriteRecordMapper extends BaseMapper<FavoriteRecord> {

    /**
     * 根据收藏夹ID分页查询文章ID列表
     *
     * @param folderId 收藏夹ID
     * @param offset   偏移量
     * @param limit    限制数
     * @return 文章ID列表
     */
    @Select("SELECT article_id FROM favorite_record WHERE folder_id = #{folderId} " +
            "AND deleted = 0 ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Long> selectArticleIdsByFolder(@Param("folderId") Long folderId,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 统计收藏夹中的文章数
     */
    @Select("SELECT COUNT(*) FROM favorite_record WHERE folder_id = #{folderId} AND deleted = 0")
    long countByFolderId(@Param("folderId") Long folderId);

}
