package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 评论Mapper
 *
 * @author juejin
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 增加评论的点赞数
     */
    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    /**
     * 减少评论的点赞数
     */
    @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(@Param("id") Long id);

    /**
     * 增加评论的回复数
     */
    @Update("UPDATE comment SET reply_count = reply_count + 1 WHERE id = #{id}")
    void incrementReplyCount(@Param("id") Long id);

    /**
     * 减少评论的回复数
     */
    @Update("UPDATE comment SET reply_count = reply_count - 1 WHERE id = #{id} AND reply_count > 0")
    void decrementReplyCount(@Param("id") Long id);

}
