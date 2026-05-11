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

// Mock 收藏集数据（API不可用时使用）
const MOCK_FOLDERS: FavoriteFolderVO[] = [
  {
    id: 1,
    name: '我的收藏',
    description: '默认收藏集',
    isPublic: false,
    articleCount: 0,
    subscriberCount: 0,
    isDefault: true,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString(),
  },
  {
    id: 2,
    name: '实用技术文章',
    description: '',
    isPublic: true,
    articleCount: 5,
    subscriberCount: 0,
    isDefault: false,
    createTime: '2024-06-12T00:00:00Z',
    updateTime: '2024-06-12T00:00:00Z',
  },
]

// Mock 收藏文章数据
const MOCK_ARTICLES: FavoriteRecordVO[] = [
  {
    id: 1,
    articleId: 1,
    title: 'SpringBoot整合minio实现断点续传、分片上传（附源码）',
    summary: '在Web开发中，大文件的上传是必不可少的功能之一。本文将介绍如何使用SpringBoot整合minio实现一个简单的大文件上传网...',
    coverImage: '',
    authorNickname: '无所谓_',
    authorAvatar: '',
    favoriteTime: '2024-06-01T10:00:00Z',
    viewCount: 5500,
    likeCount: 86,
    commentCount: 18,
    tags: [{ id: 1, name: 'Java' }, { id: 2, name: 'Vue.js' }],
  },
  {
    id: 2,
    articleId: 2,
    title: 'Day24 - 图片懒加载的原理',
    summary: '知识讲解 Why 懒加载是一种延迟加载技术，在一个长网页中存在大量图片影响加载速度和用户体验。懒加载是一种优化网页性能的方式。大量用在电商...',
    coverImage: '',
    authorNickname: '全栈然叔',
    authorAvatar: '',
    favoriteTime: '2023-05-15T08:30:00Z',
    viewCount: 5400,
    likeCount: 34,
    commentCount: 4,
    tags: [{ id: 3, name: 'JavaScript' }, { id: 4, name: '面试' }, { id: 5, name: '前端' }],
  },
  {
    id: 3,
    articleId: 3,
    title: '研究大佬写的倒计时组件(Vue)，学到了不少东西',
    summary: '本文首发于：https://github.com/bigo-frontend/blog/ 欢迎关注、转载。一、前言 入职的第一个需求是跟着一位前端大佬一...',
    coverImage: '',
    authorNickname: 'bigo前端',
    authorAvatar: '',
    favoriteTime: '2023-04-20T14:00:00Z',
    viewCount: 44000,
    likeCount: 562,
    commentCount: 80,
    tags: [{ id: 2, name: 'Vue.js' }, { id: 5, name: '前端' }],
  },
  {
    id: 4,
    articleId: 4,
    title: '33个非常实用的JavaScript一行代码，建议收藏！',
    summary: '33个实用JavaScript一行代码，建议收藏！最近在国外技术社区看到了一些关于一行代码的文章，感觉很有意思，就整理了一下...',
    coverImage: '',
    authorNickname: '掘金翻译计划',
    authorAvatar: '',
    favoriteTime: '2023-03-10T09:00:00Z',
    viewCount: 12000,
    likeCount: 230,
    commentCount: 45,
    tags: [{ id: 3, name: 'JavaScript' }],
  },
  {
    id: 5,
    articleId: 5,
    title: 'Vue3 Composition API 最佳实践指南',
    summary: '本文将深入探讨Vue3 Composition API的最佳实践，包括响应式系统、组合式函数、生命周期钩子等核心概念的实际应用...',
    coverImage: '',
    authorNickname: 'Vue开发者',
    authorAvatar: '',
    favoriteTime: '2024-01-05T16:00:00Z',
    viewCount: 8900,
    likeCount: 156,
    commentCount: 32,
    tags: [{ id: 2, name: 'Vue.js' }],
  },
]

/**
 * 获取我的收藏集列表，API失败时返回Mock数据
 */
export function getMyFolders() {
  return request.get('/favorites/folders')
    .then(r => r.data.data as FavoriteFolderVO[])
    .catch(() => {
      // API请求失败时返回Mock数据
      return Promise.resolve(MOCK_FOLDERS)
    })
}

/**
 * 获取收藏集详情（含文章列表），API失败时返回Mock数据
 * @param id - 收藏集ID
 * @returns 收藏集详情
 */
export function getFolderDetail(id: number) {
  return request.get(`/favorites/folders/${id}`)
    .then(r => r.data.data as FavoriteFolderVO)
    .catch(() => {
      const folder = MOCK_FOLDERS.find(f => f.id === id)
      if (folder) return Promise.resolve(folder)
      return Promise.reject(new Error('收藏集不存在'))
    })
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

/**
 * 获取收藏集中的文章列表，API失败时返回Mock数据
 */
export function getFolderArticles(folderId: number, params: { page?: number; size?: number }) {
  return request.get(`/favorites/folders/${folderId}/articles`, { params })
    .then(r => r.data.data as PageResult<FavoriteRecordVO>)
    .catch(() => {
      // API请求失败时返回Mock数据
      if (folderId === 2) {
        return Promise.resolve({
          list: MOCK_ARTICLES,
          total: MOCK_ARTICLES.length,
          page: params.page || 1,
          size: params.size || 10,
        } as PageResult<FavoriteRecordVO>)
      }
      return Promise.resolve({
        list: [],
        total: 0,
        page: params.page || 1,
        size: params.size || 10,
      } as PageResult<FavoriteRecordVO>)
    })
}

export function checkFavoriteStatus(articleId: number) {
  return request.get('/favorites/status', { params: { articleId } }).then(r => r.data.data as boolean)
}
