package com.juejin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.user.entity.UserOauth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserOauthMapper extends BaseMapper<UserOauth> {

    @Select("SELECT * FROM user_oauth WHERE platform = #{platform} AND oauth_id = #{oauthId} AND deleted = 0")
    UserOauth findByPlatformAndOauthId(@Param("platform") String platform, @Param("oauthId") String oauthId);

    @Select("SELECT * FROM user_oauth WHERE user_id = #{userId} AND deleted = 0")
    List<UserOauth> findByUserId(@Param("userId") Long userId);

}
