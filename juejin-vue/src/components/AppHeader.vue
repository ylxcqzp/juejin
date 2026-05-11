<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import UserAvatar from '@/components/UserAvatar.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const searchKeyword = ref('')

const showUserMenu = ref(false)

// 导航项
const navItems = computed(() => [
  { label: '首页', path: '/' },
  { label: '沸点', path: '/pins' },
])

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function onSearch() {
  const kw = searchKeyword.value.trim()
  if (kw) {
    router.push({ name: 'search', query: { q: kw } })
  }
}

function goToEditor() {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: '/editor' } })
    return
  }
  router.push({ name: 'editor' })
}

function handleLogout() {
  showUserMenu.value = false
  authStore.logout()
  router.push({ name: 'home' })
}

function goToUserProfile() {
  showUserMenu.value = false
  if (authStore.userId) {
    router.push(`/user/${authStore.userId}`)
  }
}

function goToSettings() {
  showUserMenu.value = false
  router.push('/settings')
}

/**
 * 格式化数字，大于等于10000显示为xk
 * @param n - 要格式化的数字
 * @returns 格式化后的字符串
 */
function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
</script>

<template>
  <header class="sticky top-0 z-50 bg-white border-b border-gray-100 shadow-sm">
    <div class="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between gap-4">
      <!-- 左侧：Logo + 导航 -->
      <div class="flex items-center gap-6">
        <router-link to="/" class="flex items-center gap-2 text-xl font-bold text-brand shrink-0">
          <span class="w-8 h-8 bg-brand rounded-lg flex items-center justify-center text-white font-extrabold text-sm">J</span>
          <span class="hidden sm:inline">掘金</span>
        </router-link>

        <nav class="hidden md:flex items-center gap-1">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="px-3 py-1.5 rounded-lg text-sm transition-colors"
            :class="isActive(item.path)
              ? 'text-brand bg-brand/5 font-medium'
              : 'text-text-secondary hover:text-text-primary hover:bg-gray-50'"
          >
            {{ item.label }}
          </router-link>
        </nav>
      </div>

      <!-- 中间：搜索框 -->
      <div class="flex-1 max-w-md hidden sm:block">
        <form @submit.prevent="onSearch" class="relative">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索文章、用户、标签..."
            class="w-full h-9 pl-9 pr-4 rounded-lg bg-gray-100 border border-transparent text-sm
                   placeholder:text-text-placeholder focus:bg-white focus:border-brand focus:ring-1 focus:ring-brand
                   transition-all outline-none"
          />
          <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-text-placeholder" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </form>
      </div>

      <!-- 右侧：操作 -->
      <div class="flex items-center gap-3">
        <!-- 移动端搜索图标 -->
        <button class="sm:hidden p-2 rounded-lg hover:bg-gray-100 text-text-secondary" @click="router.push({ name: 'search' })" aria-label="搜索">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </button>

        <!-- 发布按钮 -->
        <button
          @click="goToEditor"
          class="h-8 px-4 bg-brand hover:bg-brand-dark text-white text-sm font-medium rounded-lg transition-colors shrink-0"
        >
          写文章
        </button>

        <!-- 未登录 -->
        <template v-if="!authStore.isLoggedIn">
          <router-link to="/login" class="h-8 px-4 border border-brand text-brand text-sm font-medium rounded-lg hover:bg-brand/5 transition-colors shrink-0">
            登录
          </router-link>
        </template>

        <!-- 已登录 -->
        <template v-else>
          <!-- 通知 -->
          <router-link to="/notifications" class="relative p-2 rounded-lg hover:bg-gray-100 text-text-secondary" aria-label="通知">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
          </router-link>

          <!-- 用户菜单 -->
          <div class="relative">
            <button @click="showUserMenu = !showUserMenu" class="flex items-center gap-2 p-1 rounded-lg hover:bg-gray-100 transition-colors">
              <UserAvatar
                :src="authStore.user?.avatar"
                :name="authStore.user?.nickname"
                size="sm"
              />
            </button>

            <!-- 用户卡片弹框（遮罩 + 面板） -->
            <div v-if="showUserMenu">
              <div class="fixed inset-0 z-10" @click="showUserMenu = false" />
              <div
                class="absolute right-0 top-full mt-2 w-72 bg-white rounded-xl shadow-xl border border-gray-100 z-20 overflow-hidden"
              >
                <!-- 顶部用户信息 -->
                <div class="p-4">
                  <div class="flex items-center gap-3">
                    <UserAvatar
                      :src="authStore.user?.avatar"
                      :name="authStore.user?.nickname"
                      size="md"
                    />
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-semibold text-text-primary truncate">{{ authStore.user?.nickname }}</p>
                      <p class="text-xs text-text-secondary mt-0.5">
                        矿石: {{ formatCount(authStore.user?.points || 0) }}
                      </p>
                    </div>
                  </div>

                  <!-- 等级进度条 -->
                  <div class="mt-3">
                    <div class="flex items-center justify-between text-xs mb-1">
                      <span class="text-brand font-medium">掘友等级 JY.{{ authStore.user?.level || 1 }}</span>
                      <span class="text-text-secondary">{{ authStore.user?.points || 0 }} / {{ ((authStore.user?.level || 1) * 500) }} ></span>
                    </div>
                    <div class="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        class="h-full bg-brand rounded-full transition-all"
                        :style="{ width: Math.min(((authStore.user?.points || 0) % 500) / 500 * 100, 100) + '%' }"
                      />
                    </div>
                  </div>

                  <!-- 统计数据 -->
                  <div class="flex items-center justify-around mt-4 pt-3 border-t border-gray-50">
                    <div class="text-center">
                      <p class="text-sm font-bold text-text-primary">{{ formatCount(authStore.user?.followingCount || 0) }}</p>
                      <p class="text-xs text-text-secondary mt-0.5">关注</p>
                    </div>
                    <div class="text-center">
                      <p class="text-sm font-bold text-text-primary">{{ formatCount(authStore.user?.likeCount || 0) }}</p>
                      <p class="text-xs text-text-secondary mt-0.5">赞过</p>
                    </div>
                    <div class="text-center">
                      <p class="text-sm font-bold text-text-primary">{{ formatCount(authStore.user?.favoriteCount || 0) }}</p>
                      <p class="text-xs text-text-secondary mt-0.5">收藏</p>
                    </div>
                  </div>
                </div>

                <!-- 功能入口：我的主页 + 我的设置 -->
                <div class="px-4 pb-3">
                  <div class="grid grid-cols-2 gap-2">
                    <button
                      class="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm text-text-primary hover:bg-gray-50 transition-colors"
                      @click="goToUserProfile"
                    >
                      <svg class="w-4 h-4 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      我的主页
                    </button>
                    <button
                      class="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm text-text-primary hover:bg-gray-50 transition-colors"
                      @click="goToSettings"
                    >
                      <svg class="w-4 h-4 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      我的设置
                    </button>
                  </div>
                </div>

                <!-- 底部：退出登录 -->
                <div class="px-4 py-3 border-t border-gray-50">
                  <button
                    class="w-full text-left text-sm text-text-secondary hover:text-red-500 transition-colors flex items-center gap-2"
                    @click="handleLogout"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>


