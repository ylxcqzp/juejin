import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { Result } from '@/types'
import { useToast } from '@/composables/useToast'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// Toast 实例（模块级单例）
const { error: toastError } = useToast()

// 请求拦截器 — 自动携带 JWT token
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 从 Spring Boot 默认错误响应或 Result 中提取消息
function extractErrorMsg(data: any): string {
  if (!data) return '请求失败'
  if (data.message) return data.message
  if (data.error) return data.error
  return '请求失败'
}

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    if (response.data && response.data.code !== 200) {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response
  },
  (error: AxiosError<Result>) => {
    const msg = extractErrorMsg(error.response?.data)

    if (error.response?.status === 401) {
      // 清除登录态
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      // 弹框提示
      toastError(msg || '登录已过期，请重新登录')
      // 延迟跳转，让用户看到提示
      const returnUrl = encodeURIComponent(window.location.pathname + window.location.search)
      setTimeout(() => {
        window.location.href = '/login?redirect=' + returnUrl
      }, 1500)
    }

    return Promise.reject(new Error(msg))
  },
)

// ===================== Mock 模式 =====================
// 开发期间使用模拟数据，VITE_USE_MOCK=false 可关闭
const useMock = import.meta.env.VITE_USE_MOCK !== 'false'
if (useMock) {
  import('@/mock/handler').then(({ setupMock }) => {
    setupMock(request)
    console.log('[Mock] 模拟数据已启用，所有 API 请求返回虚拟数据')
  })
}

export default request
