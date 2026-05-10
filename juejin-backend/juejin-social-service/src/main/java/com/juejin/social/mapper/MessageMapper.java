package com.juejin.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juejin.social.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper
 *
 * @author juejin
 */
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY create_time ASC")
    List<Message> selectByConversationId(@Param("conversationId") Long conversationId);
}
