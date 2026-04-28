package com.juejin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);

}
