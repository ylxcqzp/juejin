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

            <!-- 下拉菜单（遮罩 + 面板） -->
            <div v-if="showUserMenu">
              <div class="fixed inset-0 z-10" @click="showUserMenu = false" />
              <div
                class="absolute right-0 top-full mt-1 w-48 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-20"
              >
                <router-link :to="`/user/${authStore.userId}`" class="flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50" @click="showUserMenu = false">
                  <UserAvatar :src="authStore.user?.avatar" :name="authStore.user?.nickname" size="sm" />
                  <span>{{ authStore.user?.nickname }}</span>
                </router-link>
                <hr class="my-1 border-gray-100" />
                <router-link to="/drafts" class="block px-4 py-2 text-sm text-text-secondary hover:bg-gray-50" @click="showUserMenu = false">草稿箱</router-link>
                <router-link to="/settings" class="block px-4 py-2 text-sm text-text-secondary hover:bg-gray-50" @click="showUserMenu = false">设置</router-link>
                <hr class="my-1 border-gray-100" />
                <button class="w-full text-left px-4 py-2 text-sm text-text-secondary hover:bg-gray-50" @click="handleLogout">退出登录</button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>


