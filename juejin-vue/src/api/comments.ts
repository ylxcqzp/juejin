import request from './request'
import type { CommentVO, PageResult } from '@/types'

export interface CommentCreateDTO {
  targetId: number
  targetType: number // 1=文章 2=沸点
  content: string
  parentId?: number
  rootId?: number
  replyUserId?: number
}

export function createComment(data: CommentCreateDTO) {
  return request.post('/comments', data).then(r => r.data.data as CommentVO)
}

export function deleteComment(commentId: number) {
  return request.delete(`/comments/${commentId}`)
}

export function toggleCommentTop(commentId: number) {
  return request.put(`/comments/${commentId}/top`)
}

export function getCommentList(params: {
  targetId: number
  targetType: number
  page?: number
  size?: number
  sortBy?: 'latest' | 'hot'
}) {
  return request.get('/comments', { params }).then(r => r.data.data as PageResult<CommentVO>)
}
