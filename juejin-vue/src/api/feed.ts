import request from './request'
import type { FeedVO, PageResult } from '@/types'

export function getFollowingFeed(params: { page?: number; size?: number }) {
  return request.get('/feed/following', { params }).then(r => r.data.data as PageResult<FeedVO>)
}

export function getRecommendFeed(params: { page?: number; size?: number }) {
  return request.get('/feed/recommend', { params }).then(r => r.data.data as PageResult<FeedVO>)
}

export function getHotFeed(params: { page?: number; size?: number }) {
  return request.get('/feed/hot', { params }).then(r => r.data.data as PageResult<FeedVO>)
}
