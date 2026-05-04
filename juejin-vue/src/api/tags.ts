import request from './request'
import type { TagVO, PageResult } from '@/types'

export function getAllTags() {
  return request.get('/tags/all').then(r => r.data.data as TagVO[])
}

export function getTagList(params: { page?: number; size?: number }) {
  return request.get('/tags', { params }).then(r => r.data.data as PageResult<TagVO>)
}

export function getTagById(id: number) {
  return request.get(`/tags/${id}`).then(r => r.data.data as TagVO)
}
