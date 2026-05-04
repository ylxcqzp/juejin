import request from './request'
import type { PageResult } from '@/types'

export interface DraftVO {
  id: number
  title: string
  content: string
  coverImage: string
  categoryId: number
  tagIds: number[]
  updateTime: string
}

export interface DraftDTO {
  title?: string
  content?: string
  coverImage?: string
  categoryId?: number
  tagIds?: number[]
}

export function getDraftList(params: { page?: number; size?: number }) {
  return request.get('/drafts', { params }).then(r => r.data.data as PageResult<DraftVO>)
}

export function getDraft(draftId: number) {
  return request.get(`/drafts/${draftId}`).then(r => r.data.data as DraftVO)
}

export function saveDraft(data: DraftDTO) {
  return request.post('/drafts', data).then(r => r.data.data as DraftVO)
}

export function autoSaveDraft(draftId: number, data: DraftDTO) {
  return request.put(`/drafts/${draftId}/auto-save`, data)
}

export function deleteDraft(draftId: number) {
  return request.delete(`/drafts/${draftId}`)
}

export function publishDraft(draftId: number) {
  return request.post(`/drafts/${draftId}/publish`)
}
