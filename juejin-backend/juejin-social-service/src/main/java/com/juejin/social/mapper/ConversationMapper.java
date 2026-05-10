package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.Conversation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话 Mapper
 *
 * @author juejin
 */
public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("SELECT * FROM conversation WHERE (user1_id = #{userId} OR user2_id = #{userId}) AND deleted = 0 ORDER BY last_message_time DESC")
    List<Conversation> selectByUserId(@Param("userId") Long userId);
}
