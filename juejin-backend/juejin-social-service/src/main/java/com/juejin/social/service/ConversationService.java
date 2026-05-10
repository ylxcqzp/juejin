package com.juejin.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.common.vo.PageResult;
import com.juejin.social.entity.Conversation;
import com.juejin.social.vo.ConversationVO;

/**
 * 会话 Service
 *
 * @author juejin
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 获取当前用户的会话列表
     */
    PageResult<ConversationVO> listConversations(Long userId, int page, int size);

    /**
     * 获取或创建与指定用户的会话
     */
    ConversationVO getOrCreateConversation(Long userId, Long otherUserId);
}
