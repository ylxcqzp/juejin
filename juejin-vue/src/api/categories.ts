import request from './request'
import type { CategoryVO, PageResult } from '@/types'

export function getAllCategories() {
  return request.get('/categories/all').then(r => r.data.data as CategoryVO[])
}

export function getCategoryList(params: { page?: number; size?: number }) {
  return request.get('/categories', { params }).then(r => r.data.data as PageResult<CategoryVO>)
}

export function getCategoryById(id: number) {
  return request.get(`/categories/${id}`).then(r => r.data.data as CategoryVO)
}
