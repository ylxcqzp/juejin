import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { Result } from '@/types'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// 请求拦截器 — 自动携带 JWT token
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    // 业务错误码（后端通过 GlobalExceptionHandler 返回 HTTP 200 + 错误 code）
    if (response.data && response.data.code !== 200) {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response
  },
  (error: AxiosError<Result>) => {
    // HTTP 错误
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    // 后端可能通过 HTTP 错误状态返回 Result body
    if (error.response?.data?.message) {
      return Promise.reject(new Error(error.response.data.message))
    }
    return Promise.reject(error)
  },
)

export default request
