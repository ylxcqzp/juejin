package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.social.dto.SendMessageDTO;
import com.juejin.social.entity.Message;
import com.juejin.social.vo.MessageVO;

/**
 * 消息 Service
 *
 * @author juejin
 */
public interface MessageService extends IService<Message> {

    /**
     * 分页获取会话消息
     */
    PageResult<MessageVO> getMessages(Long userId, Long conversationId, int page, int size);

    /**
     * 发送消息
     */
    MessageVO sendMessage(Long userId, Long conversationId, SendMessageDTO dto);

    /**
     * 标记会话为已读
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * 撤回消息（仅自己的消息，2分钟内）
     */
    void recallMessage(Long userId, Long messageId);
}
