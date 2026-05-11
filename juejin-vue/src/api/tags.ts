import request from './request'
import type { TagVO, PageResult } from '@/types'

// Mock 关注标签数据
const MOCK_TAGS: TagVO[] = [
  {
    id: 1,
    name: '前端',
    icon: '',
    description: '前端开发相关',
    articleCount: 12580,
    followerCount: 8920,
  },
  {
    id: 2,
    name: '后端',
    icon: '',
    description: '后端开发相关',
    articleCount: 8920,
    followerCount: 6540,
  },
  {
    id: 3,
    name: '面试',
    icon: '',
    description: '面试题和经验分享',
    articleCount: 4560,
    followerCount: 3210,
  },
  {
    id: 4,
    name: 'JVM',
    icon: '',
    description: 'Java虚拟机',
    articleCount: 2340,
    followerCount: 1890,
  },
  {
    id: 5,
    name: 'Spring Cloud',
    icon: '',
    description: '微服务框架',
    articleCount: 3450,
    followerCount: 2780,
  },
  {
    id: 6,
    name: 'Promise',
    icon: '',
    description: 'JavaScript异步编程',
    articleCount: 1230,
    followerCount: 980,
  },
]

/**
 * 获取标签列表
 * @param params - 查询参数
 * @returns 标签列表
 */
export function getTagList(params?: { page?: number; size?: number; keyword?: string }) {
  return request.get('/tags', { params })
    .then(r => r.data.data as PageResult<TagVO>)
    .catch(() => {
      return Promise.resolve({
        list: MOCK_TAGS,
        total: MOCK_TAGS.length,
        page: params?.page || 1,
        size: params?.size || 10,
      } as PageResult<TagVO>)
    })
}

/**
 * 获取标签详情
 * @param id - 标签ID
 * @returns 标签详情
 */
export function getTagDetail(id: number) {
  return request.get(`/tags/${id}`).then(r => r.data.data as TagVO)
}

/**
 * 获取用户关注的标签列表
 * @param userId - 用户ID
 * @returns 标签列表
 */
export function getFollowingTags(userId: number) {
  return request.get(`/users/${userId}/tags`)
    .then(r => r.data.data as TagVO[])
    .catch(() => {
      // API失败时返回Mock数据
      return Promise.resolve(MOCK_TAGS)
    })
}

/**
 * 关注标签
 * @param tagId - 标签ID
 */
export function followTag(tagId: number) {
  return request.post(`/tags/${tagId}/follow`)
    .catch(() => {
      // API失败时模拟成功
      return Promise.resolve({ data: { code: 200, message: 'success', data: null } })
    })
}

/**
 * 取消关注标签
 * @param tagId - 标签ID
 */
export function unfollowTag(tagId: number) {
  return request.delete(`/tags/${tagId}/follow`)
    .catch(() => {
      // API失败时模拟成功
      return Promise.resolve({ data: { code: 200, message: 'success', data: null } })
    })
}

/**
 * 检查是否已关注标签
 * @param tagId - 标签ID
 * @returns 是否已关注
 */
export function checkTagFollowStatus(tagId: number) {
  return request.get(`/tags/${tagId}/follow/status`).then(r => r.data.data as boolean)
}
