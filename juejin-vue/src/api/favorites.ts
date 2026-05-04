import request from './request'
import type { FavoriteFolderVO, FavoriteRecordVO, PageResult } from '@/types'

export interface FavoriteFolderDTO {
  name: string
  description?: string
  isPublic?: boolean
}

export interface FavoriteRecordDTO {
  articleId: number
  folderId?: number
}

export function getMyFolders() {
  return request.get('/favorites/folders').then(r => r.data.data as FavoriteFolderVO[])
}

export function createFolder(data: FavoriteFolderDTO) {
  return request.post('/favorites/folders', data).then(r => r.data.data as FavoriteFolderVO)
}

export function updateFolder(folderId: number, data: FavoriteFolderDTO) {
  return request.put(`/favorites/folders/${folderId}`, data)
}

export function deleteFolder(folderId: number) {
  return request.delete(`/favorites/folders/${folderId}`)
}

export function addFavorite(data: FavoriteRecordDTO) {
  return request.post('/favorites', data)
}

export function removeFavorite(articleId: number) {
  return request.delete(`/favorites/${articleId}`)
}

export function getFolderArticles(folderId: number, params: { page?: number; size?: number }) {
  return request.get(`/favorites/folders/${folderId}/articles`, { params })
    .then(r => r.data.data as PageResult<FavoriteRecordVO>)
}

export function checkFavoriteStatus(articleId: number) {
  return request.get('/favorites/status', { params: { articleId } }).then(r => r.data.data as boolean)
}
