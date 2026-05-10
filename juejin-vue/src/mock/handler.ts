// ============================================================
// Mock 请求处理器 — 拦截所有 API 调用，返回逼真模拟数据
// 通过 Axios 适配器注入，不修改业务代码
// ============================================================

import type { AxiosInstance } from 'axios'
import {
  mockArticles, mockComments, mockPins, mockPinTopics,
  mockFeeds, mockNotifications, mockFollowing, mockFollowers,
  mockFollowStatus, mockUserProfile, mockBadges,
  mockFavoriteFolders, mockFavoriteRecords,
  mockConversations, mockMessages,
  mockPoints, mockSign, mockTasks,
  mockTags, mockCategories,
  mockUsers, mockCurrentUser, mockLoginVO,
  wrapPage,
} from './data'

// 模拟网络延迟
const delay = (ms = 200) => new Promise(r => setTimeout(r, ms + Math.random() * 300))

export function setupMock(ax: AxiosInstance) {
  // 劫持所有请求
  ax.interceptors.request.use(async (config) => {
    const url = config.url || ''
    const method = config.method?.toUpperCase() || 'GET'
    const params = config.params || {}

    // 尝试匹配并返回 mock 响应
    const mockResponse = await handleMock(url, method, params, config.data)
    if (mockResponse !== null) {
      // 通过适配器返回 mock 数据
      config.adapter = async () => {
        await delay()
        return {
          data: mockResponse,
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
        }
      }
    }
    return config
  })
}

async function handleMock(url: string, method: string, params: any, body: any): Promise<any | null> {
  const page = parseInt(params?.page || '1')
  const size = parseInt(params?.size || '10')

  // ============ 用户认证 ============
  if (method === 'POST' && url === '/users/login') return { code: 200, message: 'success', data: mockLoginVO }
  if (method === 'POST' && url.includes('/auth/refresh')) return { code: 200, message: 'success', data: mockLoginVO }
  if (method === 'POST' && url.includes('/users/register')) return { code: 200, message: 'success', data: mockCurrentUser }

  // ============ 当前用户 ============
  if (method === 'GET' && url === '/users/current') return { code: 200, message: 'success', data: mockCurrentUser }
  if (method === 'GET' && url === '/users/profile') return { code: 200, message: 'success', data: mockUserProfile }
  if (method === 'PUT' && url === '/users/profile') return { code: 200, message: 'success' }
  if (method === 'PUT' && url === '/users/password') return { code: 200, message: 'success' }
  if (method === 'PUT' && url === '/users/tags') return { code: 200, message: 'success' }
  if (method === 'PUT' && url === '/users/social-links') return { code: 200, message: 'success' }
  if (method === 'GET' && url === '/users/points') return { code: 200, message: 'success', data: mockPoints }
  if (method === 'GET' && url === '/users/badges') return { code: 200, message: 'success', data: [] }
  if (method === 'GET' && url === '/users/badges/all') return { code: 200, message: 'success', data: mockBadges }
  if (method === 'GET' && url === '/users/privacy') return { code: 200, message: 'success', data: mockUserProfile.privacy }
  if (method === 'PUT' && url === '/users/privacy') return { code: 200, message: 'success' }
  if (method === 'POST' && url.includes('/users/bind/')) return { code: 200, message: 'success' }

  // ============ 用户信息（公开） ============
  const userMatch = url.match(/^\/users\/(\d+)$/)
  if (method === 'GET' && userMatch) {
    const user = mockUsers.find(u => u.id === Number(userMatch[1])) || mockCurrentUser
    return { code: 200, message: 'success', data: user }
  }
  if (method === 'GET' && url.match(/^\/users\/\d+\/exists$/)) return { code: 200, message: 'success', data: true }
  if (method === 'GET' && url.match(/^\/users\/\d+\/profile$/)) return { code: 200, message: 'success', data: mockUserProfile }

  // ============ 关注 ============
  if (method === 'POST' && url.match(/^\/users\/\d+\/follow$/)) return { code: 200, message: 'success' }
  if (method === 'DELETE' && url.match(/^\/users\/\d+\/follow$/)) return { code: 200, message: 'success' }
  if (method === 'GET' && url.match(/^\/users\/\d+\/following/)) return { code: 200, message: 'success', data: wrapPage(mockFollowing, page, size) }
  if (method === 'GET' && url.match(/^\/users\/\d+\/followers/)) return { code: 200, message: 'success', data: wrapPage(mockFollowers, page, size) }
  if (method === 'DELETE' && url.match(/^\/users\/followers\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'GET' && url.match(/^\/users\/\d+\/follow\/status$/)) return { code: 200, message: 'success', data: mockFollowStatus }

  // ============ 文章 ============
  if (method === 'POST' && url === '/articles') {
    const newArticle = { ...mockArticles[0], id: Date.now(), title: body?.title || '新文章', status: 1, publishTime: '', viewCount: 0, likeCount: 0, commentCount: 0, favoriteCount: 0, createTime: new Date().toISOString() }
    return { code: 200, message: 'success', data: newArticle }
  }
  if (method === 'GET' && url.match(/^\/articles\/\d+$/)) {
    const articleId = Number(url.split('/').pop())
    const article = mockArticles.find(a => a.id === articleId) || mockArticles[0]
    return { code: 200, message: 'success', data: article }
  }
  if (method === 'PUT' && url.match(/^\/articles\/\d+$/)) return { code: 200, message: 'success', data: mockArticles[0] }
  if (method === 'DELETE' && url.match(/^\/articles\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'GET' && url === '/articles') {
    const categoryId = parseInt(params?.categoryId)
    let list = mockArticles.filter(a => a.status === 2)
    if (categoryId) list = list.filter(a => a.categoryId === categoryId)
    return { code: 200, message: 'success', data: wrapPage(list, page, size) }
  }
  if (method === 'GET' && url.match(/^\/articles\/user\/\d+$/)) {
    const userId = Number(url.split('/').pop())
    const userArticles = mockArticles.filter(a => a.authorId === userId)
    return { code: 200, message: 'success', data: wrapPage(userArticles, page, size) }
  }
  if (method === 'GET' && url === '/articles/hot') {
    return { code: 200, message: 'success', data: mockArticles.filter(a => a.status === 2).slice(0, 6) }
  }
  if (method === 'GET' && url === '/articles/search') {
    const keyword = (params?.keyword || '').toLowerCase()
    const results = mockArticles.filter(a => a.status === 2 && (a.title.toLowerCase().includes(keyword) || a.summary?.toLowerCase().includes(keyword)))
    return { code: 200, message: 'success', data: wrapPage(results, page, size) }
  }

  // ============ 评论 ============
  if (method === 'POST' && url === '/comments') return { code: 200, message: 'success', data: mockComments[0] }
  if (method === 'DELETE' && url.match(/^\/comments\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'PUT' && url.match(/^\/comments\/\d+\/top$/)) return { code: 200, message: 'success' }
  if (method === 'GET' && url === '/comments') return { code: 200, message: 'success', data: wrapPage(mockComments, page, size) }

  // ============ 点赞 ============
  if (method === 'POST' && url === '/likes') return { code: 200, message: 'success' }
  if (method === 'GET' && url === '/likes/status') return { code: 200, message: 'success', data: Math.random() > 0.5 }

  // ============ 收藏 ============
  if (method === 'GET' && url === '/favorites/folders') return { code: 200, message: 'success', data: mockFavoriteFolders }
  if (method === 'POST' && url.match(/^\/favorites\/folders(\/\d+)?$/)) return { code: 200, message: 'success', data: mockFavoriteFolders[0] }
  if (method === 'PUT' && url.match(/^\/favorites\/folders\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'DELETE' && url.match(/^\/favorites\/folders\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'POST' && url === '/favorites') return { code: 200, message: 'success' }
  if (method === 'DELETE' && url.match(/^\/favorites\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'GET' && url.match(/^\/favorites\/folders\/\d+\/articles/)) return { code: 200, message: 'success', data: wrapPage(mockFavoriteRecords, page, size) }
  if (method === 'GET' && url === '/favorites/status') return { code: 200, message: 'success', data: Math.random() > 0.5 }

  // ============ 沸点 ============
  if (method === 'POST' && url === '/pins') {
    const newPin = { ...mockPins[0], id: Date.now(), content: body?.content || '', createTime: new Date().toISOString() }
    return { code: 200, message: 'success', data: newPin }
  }
  if (method === 'GET' && url === '/pins') return { code: 200, message: 'success', data: wrapPage(mockPins, page, size) }
  if (method === 'GET' && url === '/pins/following') return { code: 200, message: 'success', data: wrapPage(mockPins.slice(0, 3), page, size) }
  if (method === 'GET' && url.match(/^\/pins\/topic\/\d+$/)) return { code: 200, message: 'success', data: wrapPage(mockPins, page, size) }
  if (method === 'GET' && url === '/pins/topics/hot') return { code: 200, message: 'success', data: mockPinTopics }

  // ============ Feed ============
  if (method === 'GET' && url === '/feed/recommend') return { code: 200, message: 'success', data: wrapPage(mockFeeds, page, size) }
  if (method === 'GET' && url === '/feed/following') return { code: 200, message: 'success', data: wrapPage(mockFeeds.slice(0, 5), page, size) }
  if (method === 'GET' && url === '/feed/hot') return { code: 200, message: 'success', data: wrapPage(mockFeeds.filter(f => f.type === 'article'), page, size) }

  // ============ 通知 ============
  if (method === 'GET' && url === '/notifications') return { code: 200, message: 'success', data: wrapPage(mockNotifications, page, size) }
  if (method === 'GET' && url === '/notifications/unread-count') return { code: 200, message: 'success', data: mockNotifications.filter(n => !n.isRead).length }
  if (method === 'PUT' && url === '/notifications/read-all') return { code: 200, message: 'success' }

  // ============ 标签 ============
  if (method === 'GET' && url === '/tags/all') return { code: 200, message: 'success', data: mockTags }
  if (method === 'GET' && url === '/tags') return { code: 200, message: 'success', data: wrapPage(mockTags, page, size) }
  if (method === 'GET' && url.match(/^\/tags\/\d+$/)) return { code: 200, message: 'success', data: mockTags[0] }

  // ============ 分类 ============
  if (method === 'GET' && url === '/categories/all') return { code: 200, message: 'success', data: mockCategories }
  if (method === 'GET' && url === '/categories') return { code: 200, message: 'success', data: wrapPage(mockCategories, page, size) }
  if (method === 'GET' && url.match(/^\/categories\/\d+$/)) return { code: 200, message: 'success', data: mockCategories[0] }

  // ============ 草稿 ============
  if (method === 'GET' && url === '/drafts') {
    const drafts = [{ id: 201, title: '未完成的React性能优化文章', content: '# React性能优化\n\n还在写...', coverImage: '', categoryId: 1, tagIds: [2], updateTime: '2025-05-03T10:30:00' }]
    return { code: 200, message: 'success', data: wrapPage(drafts, page, size) }
  }
  if (method === 'GET' && url.match(/^\/drafts\/\d+$/)) return { code: 200, message: 'success', data: { id: 201, title: '未完成的React性能优化文章', content: '# React性能优化\n\n还在写...', coverImage: '', categoryId: 1, tagIds: [2], updateTime: '2025-05-03T10:30:00' } }
  if (method === 'POST' && url === '/drafts') return { code: 200, message: 'success', data: { id: Date.now() } }
  if (method === 'PUT' && url.match(/^\/drafts\/\d+\/auto-save$/)) return { code: 200, message: 'success' }
  if (method === 'DELETE' && url.match(/^\/drafts\/\d+$/)) return { code: 200, message: 'success' }
  if (method === 'POST' && url.match(/^\/drafts\/\d+\/publish$/)) return { code: 200, message: 'success' }

  // ============ 签到 ============
  if (method === 'POST' && url === '/signs') return { code: 200, message: 'success', data: mockSign }
  if (method === 'GET' && url === '/signs/status') return { code: 200, message: 'success', data: mockSign }

  // ============ 任务 ============
  if (method === 'GET' && url === '/tasks') return { code: 200, message: 'success', data: mockTasks }
  if (method === 'GET' && url.match(/^\/tasks\/type\/\w+$/)) return { code: 200, message: 'success', data: mockTasks }
  if (method === 'POST' && url.match(/^\/tasks\/\d+\/claim$/)) return { code: 200, message: 'success', data: { ...mockTasks[1], isClaimed: true } }

  // ============ 文件上传 ============
  if (method === 'POST' && url === '/uploads/images') {
    return { code: 200, message: 'success', data: `https://picsum.photos/800/400?random=${Date.now()}` }
  }

  // ============ 私信 ============
  if (method === 'GET' && url === '/conversations') return { code: 200, message: 'success', data: wrapPage(mockConversations, page, size) }
  if (method === 'POST' && url === '/conversations') {
    const otherUserId = Number(params?.otherUserId)
    const user = mockUsers.find(u => u.id === otherUserId)
    const conv = { id: Date.now(), otherUserId, otherUserNickname: user?.nickname || '未知用户', otherUserAvatar: '', lastMessage: '', lastMessageTime: new Date().toISOString(), unreadCount: 0 }
    return { code: 200, message: 'success', data: conv }
  }
  if (method === 'GET' && url.match(/^\/conversations\/\d+\/messages$/)) {
    const convId = Number(url.split('/')[2])
    return { code: 200, message: 'success', data: wrapPage(mockMessages.filter(m => m.conversationId === convId), page, size * 10) }
  }
  if (method === 'POST' && url.match(/^\/conversations\/\d+\/messages$/)) {
    const newMsg = { id: Date.now(), conversationId: Number(url.split('/')[2]), senderId: 1, senderNickname: '张三前端', senderAvatar: '', content: body?.content || '', contentType: 1, isRead: false, isRecalled: false, createTime: new Date().toISOString() }
    return { code: 200, message: 'success', data: newMsg }
  }
  if (method === 'PUT' && url.match(/^\/conversations\/\d+\/read$/)) return { code: 200, message: 'success' }
  if (method === 'PUT' && url.match(/^\/conversations\/\d+\/messages\/\d+\/recall$/)) return { code: 200, message: 'success' }

  return null
}
