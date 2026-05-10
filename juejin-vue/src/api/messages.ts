import request from './request'
import type { ConversationVO, MessageVO, PageResult } from '@/types'

export interface SendMessageDTO {
  receiverId: number
  content: string
  contentType?: number
}

/** 获取会话列表 */
export function getConversations(params: { page?: number; size?: number }) {
  return request.get('/conversations', { params })
    .then(r => r.data.data as PageResult<ConversationVO>)
}

/** 获取或创建与指定用户的会话 */
export function getOrCreateConversation(otherUserId: number) {
  return request.post('/conversations', null, { params: { otherUserId } })
    .then(r => r.data.data as ConversationVO)
}

/** 获取会话消息列表 */
export function getMessages(conversationId: number, params: { page?: number; size?: number }) {
  return request.get(`/conversations/${conversationId}/messages`, { params })
    .then(r => r.data.data as PageResult<MessageVO>)
}

/** 发送消息 */
export function sendMessage(conversationId: number, data: SendMessageDTO) {
  return request.post(`/conversations/${conversationId}/messages`, data)
    .then(r => r.data.data as MessageVO)
}

/** 标记会话已读 */
export function markAsRead(conversationId: number) {
  return request.put(`/conversations/${conversationId}/read`)
}

/** 撤回消息 */
export function recallMessage(conversationId: number, messageId: number) {
  return request.put(`/conversations/${conversationId}/messages/${messageId}/recall`)
}
