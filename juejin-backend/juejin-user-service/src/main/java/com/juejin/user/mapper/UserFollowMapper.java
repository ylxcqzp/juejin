package com.juejin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.user.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("SELECT uf.*, u.nickname, u.avatar, u.bio, u.level " +
            "FROM user_follow uf " +
            "LEFT JOIN user u ON uf.following_id = u.id " +
            "WHERE uf.user_id = #{userId} AND uf.status = 1 AND uf.deleted = 0 " +
            "ORDER BY uf.create_time DESC")
    List<FollowUserRow> selectFollowingList(@Param("userId") Long userId);

    @Select("SELECT uf.*, u.nickname, u.avatar, u.bio, u.level " +
            "FROM user_follow uf " +
            "LEFT JOIN user u ON uf.user_id = u.id " +
            "WHERE uf.following_id = #{userId} AND uf.status = 1 AND uf.deleted = 0 " +
            "ORDER BY uf.create_time DESC")
    List<FollowUserRow> selectFollowerList(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_follow f1 " +
            "INNER JOIN user_follow f2 ON f1.user_id = f2.following_id AND f1.following_id = f2.user_id " +
            "WHERE f1.user_id = #{userId} AND f1.following_id = #{targetId} " +
            "AND f1.status = 1 AND f1.deleted = 0 AND f2.status = 1 AND f2.deleted = 0")
    int countMutualFollow(@Param("userId") Long userId, @Param("targetId") Long targetId);

    @Select("SELECT * FROM user_follow WHERE user_id = #{userId} AND following_id = #{followingId} AND status = 1 AND deleted = 0")
    UserFollow checkFollowStatus(@Param("userId") Long userId, @Param("followingId") Long followingId);

}
