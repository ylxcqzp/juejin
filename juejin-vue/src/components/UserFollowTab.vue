<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFollowingList, getFollowerList, followUser, unfollowUser } from '@/api/users'
import { getFollowingTags, followTag, unfollowTag } from '@/api/tags'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { FollowUserVO, TagVO } from '@/types'

const props = defineProps<{
  userId: number
}>()

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// 子Tab类型：following-关注的用户 followers-关注者 tags-关注标签
type SubTab = 'following' | 'followers' | 'tags'
const activeSubTab = ref<SubTab>('following')

// 根据 URL query 初始化子Tab
const subTabFromQuery = route.query.subTab as string
if (subTabFromQuery && ['following', 'followers', 'tags'].includes(subTabFromQuery)) {
  activeSubTab.value = subTabFromQuery as SubTab
}

// 数据状态
const followingUsers = ref<FollowUserVO[]>([])
const followers = ref<FollowUserVO[]>([])
const followingTags = ref<TagVO[]>([])
const loading = ref(false)

// 关注/取消关注操作锁
const { isSubmitting, withLock } = useSubmitLock()

// 子Tab配置
const subTabs = [
  { key: 'following' as SubTab, label: '关注的用户' },
  { key: 'followers' as SubTab, label: '关注者' },
  { key: 'tags' as SubTab, label: '关注标签' },
]

/**
 * 加载数据
 */
async function loadData() {
  loading.value = true
  try {
    if (activeSubTab.value === 'following') {
      const data = await getFollowingList(props.userId, 1, 50)
      followingUsers.value = data.list.map(user => ({ ...user, isFollowing: true }))
    } else if (activeSubTab.value === 'followers') {
      const data = await getFollowerList(props.userId, 1, 50)
      // 关注者列表需要检查当前用户是否关注了这些人
      followers.value = data.list.map(user => ({ ...user, isFollowing: false }))
    } else if (activeSubTab.value === 'tags') {
      const data = await getFollowingTags(props.userId)
      followingTags.value = data.map(tag => ({ ...tag, isFollowing: true }))
    }
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理用户关注/取消关注
 * @param user - 用户对象
 * @param isFollowingList - 是否是关注的用户列表（true表示在"关注的用户"列表中）
 */
async function handleFollowUser(user: FollowUserVO & { isFollowing?: boolean }, isFollowingList: boolean) {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  const wasFollowing = user.isFollowing
  // 先更新UI状态
  user.isFollowing = !wasFollowing

  try {
    if (wasFollowing) {
      await unfollowUser(user.userId)
      // 关注的用户列表中取消关注后，按钮变成"+ 关注"，数据保留在列表中
      // 只有刷新页面后才会从列表中消失（因为API返回的是已关注用户）
    } else {
      await followUser(user.userId)
    }
  } catch (e: unknown) {
    // 请求失败，恢复状态
    user.isFollowing = wasFollowing
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

/**
 * 处理标签关注/取消关注
 * @param tag - 标签对象
 */
async function handleFollowTag(tag: TagVO & { isFollowing?: boolean }) {
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  const wasFollowing = tag.isFollowing
  // 先更新UI状态
  tag.isFollowing = !wasFollowing

  try {
    if (wasFollowing) {
      await unfollowTag(tag.id)
      // 取消关注后，按钮变成"+ 关注"，数据保留在列表中
      // 只有刷新页面后才会从列表中消失
    } else {
      await followTag(tag.id)
    }
  } catch (e: unknown) {
    // 请求失败，恢复状态
    tag.isFollowing = wasFollowing
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

/**
 * 导航到用户主页
 * @param userId - 用户ID
 */
function goToUser(userId: number) {
  router.push(`/user/${userId}`)
}

/**
 * 导航到标签页面
 * @param tagId - 标签ID
 */
function goToTag(tagId: number) {
  router.push(`/tag/${tagId}`)
}

// 监听子Tab变化
watch(activeSubTab, () => {
  // 更新 URL query
  router.replace({ query: { ...route.query, subTab: activeSubTab.value } })
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
    <!-- 头部：左侧标题 + 右侧子Tab -->
    <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
      <!-- 左侧标题 -->
      <h2 class="text-base font-semibold text-text-primary">关注</h2>

      <!-- 右侧子Tab -->
      <div class="flex items-center gap-6">
        <button
          v-for="tab in subTabs"
          :key="tab.key"
          class="text-sm transition-colors relative pb-1"
          :class="activeSubTab === tab.key ? 'text-text-primary font-medium' : 'text-text-secondary hover:text-text-primary'"
          @click="activeSubTab = tab.key"
        >
          {{ tab.label }}
          <span
            v-if="activeSubTab === tab.key"
            class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand rounded-full"
          />
        </button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center items-center py-16">
      <svg class="animate-spin w-6 h-6 text-brand" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <!-- 关注的用户列表 -->
    <div v-else-if="activeSubTab === 'following'" class="divide-y divide-gray-50">
      <div
        v-for="user in followingUsers"
        :key="user.userId"
        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50/50 transition-colors"
      >
        <!-- 左侧：头像 + 信息 -->
        <div class="flex items-center gap-3 cursor-pointer" @click="goToUser(user.userId)">
          <!-- 头像 -->
          <div class="w-12 h-12 rounded-full bg-gray-100 overflow-hidden flex-shrink-0">
            <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname" class="w-full h-full object-cover" />
            <span v-else class="w-full h-full flex items-center justify-center text-base font-bold text-gray-400">
              {{ user.nickname?.charAt(0) || 'U' }}
            </span>
          </div>

          <!-- 用户信息 -->
          <div>
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-text-primary hover:text-brand transition-colors">{{ user.nickname }}</span>
              <!-- 等级标识 -->
              <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-500">
                Lv.{{ (user as any).level || 5 }}
              </span>
            </div>
            <p v-if="user.bio" class="text-xs text-text-secondary mt-0.5">{{ user.bio }}</p>
            <p v-else class="text-xs text-text-placeholder mt-0.5">后端开发工程师</p>
          </div>
        </div>

        <!-- 右侧：已关注按钮 -->
        <button
          class="px-5 py-1.5 rounded text-sm font-medium transition-all duration-200 min-w-[80px]"
          :class="user.isFollowing
            ? 'bg-[#8BC34A] text-white hover:bg-[#7CB342]'
            : 'bg-gray-100 text-text-secondary hover:bg-gray-200'"
          @click="handleFollowUser(user, true)"
        >
          {{ user.isFollowing ? '已关注' : '+ 关注' }}
        </button>
      </div>

      <!-- 空状态 -->
      <div v-if="followingUsers.length === 0" class="flex flex-col items-center justify-center py-16">
        <div class="w-32 h-32 mb-4">
          <!-- 掘金风格空状态插画 -->
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <!-- 身体 -->
            <ellipse cx="60" cy="75" rx="25" ry="30" fill="#E3F2FD"/>
            <!-- 头 -->
            <circle cx="60" cy="45" r="20" fill="#E3F2FD"/>
            <!-- 眼睛 -->
            <circle cx="54" cy="42" r="2" fill="#90CAF9"/>
            <circle cx="66" cy="42" r="2" fill="#90CAF9"/>
            <!-- 嘴巴 -->
            <path d="M56 50 Q60 53 64 50" stroke="#90CAF9" stroke-width="1.5" fill="none"/>
            <!-- 星星装饰 -->
            <circle cx="85" cy="35" r="3" fill="#FFD54F"/>
            <circle cx="35" cy="40" r="2" fill="#81D4FA"/>
            <circle cx="90" cy="60" r="2" fill="#81D4FA"/>
            <!-- 小草装饰 -->
            <path d="M30 90 Q32 80 35 85" stroke="#A5D6A7" stroke-width="2" fill="none"/>
            <path d="M85 90 Q87 80 90 85" stroke="#A5D6A7" stroke-width="2" fill="none"/>
            <path d="M45 95 Q47 85 50 90" stroke="#A5D6A7" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <p class="text-sm text-text-placeholder">这里什么都没有</p>
      </div>
    </div>

    <!-- 关注者列表 -->
    <div v-else-if="activeSubTab === 'followers'" class="divide-y divide-gray-50">
      <div
        v-for="user in followers"
        :key="user.userId"
        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50/50 transition-colors"
      >
        <!-- 左侧：头像 + 信息 -->
        <div class="flex items-center gap-3 cursor-pointer" @click="goToUser(user.userId)">
          <!-- 头像 -->
          <div class="w-12 h-12 rounded-full bg-gray-100 overflow-hidden flex-shrink-0">
            <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname" class="w-full h-full object-cover" />
            <span v-else class="w-full h-full flex items-center justify-center text-base font-bold text-gray-400">
              {{ user.nickname?.charAt(0) || 'U' }}
            </span>
          </div>

          <!-- 用户信息 -->
          <div>
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-text-primary hover:text-brand transition-colors">{{ user.nickname }}</span>
              <!-- 等级标识 -->
              <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-500">
                Lv.{{ (user as any).level || 5 }}
              </span>
            </div>
            <p v-if="user.bio" class="text-xs text-text-secondary mt-0.5">{{ user.bio }}</p>
            <p v-else class="text-xs text-text-placeholder mt-0.5">后端开发工程师</p>
          </div>
        </div>

        <!-- 右侧：关注/已关注按钮 -->
        <button
          class="px-5 py-1.5 rounded text-sm font-medium transition-all duration-200 min-w-[80px]"
          :class="user.isFollowing
            ? 'bg-[#8BC34A] text-white hover:bg-[#7CB342]'
            : 'bg-gray-100 text-text-secondary hover:bg-gray-200'"
          @click="handleFollowUser(user, false)"
        >
          {{ user.isFollowing ? '已关注' : '+ 关注' }}
        </button>
      </div>

      <!-- 空状态 -->
      <div v-if="followers.length === 0" class="flex flex-col items-center justify-center py-16">
        <div class="w-32 h-32 mb-4">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <ellipse cx="60" cy="75" rx="25" ry="30" fill="#E3F2FD"/>
            <circle cx="60" cy="45" r="20" fill="#E3F2FD"/>
            <circle cx="54" cy="42" r="2" fill="#90CAF9"/>
            <circle cx="66" cy="42" r="2" fill="#90CAF9"/>
            <path d="M56 50 Q60 53 64 50" stroke="#90CAF9" stroke-width="1.5" fill="none"/>
            <circle cx="85" cy="35" r="3" fill="#FFD54F"/>
            <circle cx="35" cy="40" r="2" fill="#81D4FA"/>
            <circle cx="90" cy="60" r="2" fill="#81D4FA"/>
            <path d="M30 90 Q32 80 35 85" stroke="#A5D6A7" stroke-width="2" fill="none"/>
            <path d="M85 90 Q87 80 90 85" stroke="#A5D6A7" stroke-width="2" fill="none"/>
            <path d="M45 95 Q47 85 50 90" stroke="#A5D6A7" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <p class="text-sm text-text-placeholder">这里什么都没有</p>
      </div>
    </div>

    <!-- 关注标签列表 -->
    <div v-else-if="activeSubTab === 'tags'" class="divide-y divide-gray-50">
      <div
        v-for="tag in followingTags"
        :key="tag.id"
        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50/50 transition-colors"
      >
        <!-- 左侧：标签图标 + 名称 -->
        <div class="flex items-center gap-3 cursor-pointer" @click="goToTag(tag.id)">
          <!-- 标签图标 -->
          <div class="w-12 h-12 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0 flex items-center justify-center">
            <img v-if="tag.icon" :src="tag.icon" :alt="tag.name" class="w-full h-full object-cover" />
            <span v-else class="text-lg font-bold text-brand">{{ tag.name?.charAt(0) || 'T' }}</span>
          </div>

          <!-- 标签名称 -->
          <span class="text-sm font-medium text-text-primary hover:text-brand transition-colors">{{ tag.name }}</span>
        </div>

        <!-- 右侧：已关注按钮 -->
        <button
          class="px-5 py-1.5 rounded text-sm font-medium transition-all duration-200 min-w-[80px]"
          :class="tag.isFollowing
            ? 'bg-[#8BC34A] text-white hover:bg-[#7CB342]'
            : 'bg-gray-100 text-text-secondary hover:bg-gray-200'"
          @click="handleFollowTag(tag)"
        >
          {{ tag.isFollowing ? '已关注' : '+ 关注' }}
        </button>
      </div>

      <!-- 空状态 -->
      <div v-if="followingTags.length === 0" class="flex flex-col items-center justify-center py-16">
        <div class="w-32 h-32 mb-4">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <ellipse cx="60" cy="75" rx="25" ry="30" fill="#E3F2FD"/>
            <circle cx="60" cy="45" r="20" fill="#E3F2FD"/>
            <circle cx="54" cy="42" r="2" fill="#90CAF9"/>
            <circle cx="66" cy="42" r="2" fill="#90CAF9"/>
            <path d="M56 50 Q60 53 64 50" stroke="#90CAF9" stroke-width="1.5" fill="none"/>
            <circle cx="85" cy="35" r="3" fill="#FFD54F"/>
            <circle cx="35" cy="40" r="2" fill="#81D4FA"/>
            <circle cx="90" cy="60" r="2" fill="#81D4FA"/>
            <path d="M30 90 Q32 80 35 85" stroke="#A5D6A7" stroke-width="2" fill="none"/>
            <path d="M85 90 Q87 80 90 85" stroke="#A5D6A7" stroke-width="2" fill="none"/>
            <path d="M45 95 Q47 85 50 90" stroke="#A5D6A7" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <p class="text-sm text-text-placeholder">这里什么都没有</p>
      </div>
    </div>
  </div>
</template>
