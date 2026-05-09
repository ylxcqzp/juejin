import request from './request'
import type { ArticleVO, PageResult, PageParam } from '@/types'

export interface ArticleCreateDTO {
  title: string
  content: string
  contentHtml?: string
  summary?: string
  coverImage?: string
  categoryId?: number
  tagIds?: number[]
  isOriginal?: number  // 0=转载 1=原创
  scheduledTime?: string
}

export interface ArticleUpdateDTO {
  title: string
  content: string
  contentHtml?: string
  summary?: string
  coverImage?: string
  categoryId?: number
  tagIds?: number[]
  isOriginal?: number  // 0=转载 1=原创
}

export function createArticle(data: ArticleCreateDTO) {
  return request.post('/articles', data).then(r => r.data.data as ArticleVO)
}

export function updateArticle(id: number, data: ArticleUpdateDTO) {
  return request.put(`/articles/${id}`, data).then(r => r.data.data as ArticleVO)
}

export function getArticle(id: number) {
  return request.get(`/articles/${id}`).then(r => r.data.data as ArticleVO)
}

export function deleteArticle(id: number) {
  return request.delete(`/articles/${id}`)
}

export function getArticleList(params: PageParam & { categoryId?: number; sortBy?: 'latest' | 'hot' | 'recommend' }) {
  return request.get('/articles', { params }).then(r => r.data.data as PageResult<ArticleVO>)
}

export function getUserArticles(userId: number, params: PageParam) {
  return request.get(`/articles/user/${userId}`, { params }).then(r => r.data.data as PageResult<ArticleVO>)
}

export function getHotArticles() {
  return request.get('/articles/hot').then(r => r.data.data as ArticleVO[])
}

export function searchArticles(params: { keyword: string } & PageParam) {
  return request.get('/articles/search', { params }).then(r => r.data.data as PageResult<ArticleVO>)
}

export function submitReview(articleId: number) {
  return request.post(`/articles/${articleId}/review`)
}

export function approveArticle(articleId: number) {
  return request.post(`/articles/${articleId}/approve`)
}

export function rejectArticle(articleId: number, reason: string) {
  return request.post(`/articles/${articleId}/reject`, { reason })
}

// ==================== 草稿 API（合并到 article.status=0） ====================

export interface DraftDTO {
  title?: string
  content?: string
  coverImage?: string
  categoryId?: number
  tagIds?: number[]
}

export function getDraftList(params: { page?: number; size?: number }) {
  return request.get('/articles/drafts', { params }).then(r => r.data.data as PageResult<ArticleVO>)
}

export function getDraft(draftId: number) {
  return request.get(`/articles/drafts/${draftId}`).then(r => r.data.data as ArticleVO)
}

export function createDraftArticle(data: DraftDTO) {
  return request.post('/articles/drafts', data).then(r => r.data.data as ArticleVO)
}

export function updateDraftArticle(draftId: number, data: DraftDTO) {
  return request.put(`/articles/drafts/${draftId}`, data).then(r => r.data.data as ArticleVO)
}

export function deleteDraftArticle(draftId: number) {
  return request.delete(`/articles/drafts/${draftId}`)
}
