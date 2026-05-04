import request from './request'
import type { NotificationVO, PageResult } from '@/types'

export function getNotifications(params: { page?: number; size?: number; type?: string }) {
  return request.get('/notifications', { params }).then(r => r.data.data as PageResult<NotificationVO>)
}

export function getUnreadCount() {
  return request.get('/notifications/unread-count').then(r => r.data.data as number)
}

export function markAllRead() {
  return request.put('/notifications/read-all')
}
