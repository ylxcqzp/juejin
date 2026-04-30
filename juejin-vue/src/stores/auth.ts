import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO } from '@/types'
import request from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref<UserVO | null>(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id ?? null)

  async function login(account: string, password: string, loginType: 'email' | 'phone' = 'email') {
    // 根据登录类型映射后端期望的字段名
    const body = loginType === 'phone'
      ? { phone: account, password, loginType }
      : { email: account, password, loginType }

    const res = await request.post('/users/login', body)

    // 检查业务错误码（后端异常通过 code !== 200 返回，不是 HTTP 错误状态）
    if (res.data.code !== 200) {
      throw new Error(res.data.message || '登录失败')
    }

    const { accessToken, refreshToken: rt, userId: uid } = res.data.data
    token.value = accessToken
    refreshToken.value = rt
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', rt)
    await fetchUser(uid)
  }

  async function refreshLogin() {
    if (!refreshToken.value) throw new Error('No refresh token')
    const res = await request.post('/auth/refresh', null, {
      headers: { Authorization: `Bearer ${refreshToken.value}` },
    })
    if (res.data.code !== 200) {
      throw new Error(res.data.message || '刷新失败')
    }
    const { accessToken, refreshToken: rt } = res.data.data
    token.value = accessToken
    refreshToken.value = rt
    localStorage.setItem('token', accessToken)
    localStorage.setItem('refreshToken', rt)
  }

  async function fetchUser(uid?: number) {
    const id = uid ?? userId.value
    const res = await request.get(`/users/${id}`)
    if (res.data.code !== 200) {
      throw new Error(res.data.message || '获取用户信息失败')
    }
    user.value = res.data.data
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return { token, refreshToken, user, isLoggedIn, userId, login, refreshLogin, fetchUser, logout }
})
