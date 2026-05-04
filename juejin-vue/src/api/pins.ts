import request from './request'
import type { PinVO, PinTopicVO, PageResult } from '@/types'

export interface PinCreateDTO {
  content: string
  images?: string
  linkUrl?: string
  topicId?: number
}

export function createPin(data: PinCreateDTO) {
  return request.post('/pins', data).then(r => r.data.data as PinVO)
}

export function getPinList(params: { page?: number; size?: number; sortBy?: 'latest' | 'hot' }) {
  return request.get('/pins', { params }).then(r => r.data.data as PageResult<PinVO>)
}

export function getFollowingPins(params: { page?: number; size?: number }) {
  return request.get('/pins/following', { params }).then(r => r.data.data as PageResult<PinVO>)
}

export function getPinsByTopic(topicId: number, params: { page?: number; size?: number }) {
  return request.get(`/pins/topic/${topicId}`, { params }).then(r => r.data.data as PageResult<PinVO>)
}

export function getHotTopics(limit?: number) {
  return request.get('/pins/topics/hot', { params: { limit } }).then(r => r.data.data as PinTopicVO[])
}
