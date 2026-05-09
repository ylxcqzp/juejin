<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, getUnreadCount, markAllRead } from '@/api/notifications'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { NotificationVO } from '@/types'

const router = useRouter()
const toast = useToast()

const notifications = ref<NotificationVO[]>([])
const unreadCount = ref(0)
const total = ref(0)
const loading = ref(true)
const typeFilter = ref<string>('')

const { isSubmitting: markingRead, withLock: withMarkReadLock } = useSubmitLock()

const filters = [
  { key: '', label: '全部' },
  { key: 'like', label: '点赞' },
  { key: 'comment', label: '评论' },
  { key: 'follow', label: '关注' },
  { key: 'system', label: '系统' },
]

async function loadNotifications() {
  loading.value = true
  try {
    const [notifData, unread] = await Promise.all([
      getNotifications({ page: 1, size: 50, type: typeFilter.value || undefined }),
      getUnreadCount(),
    ])
    notifications.value = notifData.list
    total.value = notifData.total
    unreadCount.value = unread
  } catch (e: unknown) {
    notifications.value = []
    toast.error(e instanceof Error ? e.message : '通知加载失败')
  } finally { loading.value = false }
}

async function handleMarkAllRead() {
  await withMarkReadLock(async () => {
    await markAllRead()
    unreadCount.value = 0
    notifications.value.forEach(n => { n.isRead = true })
  })
}

function switchFilter(key: string) {
  typeFilter.value = key
  loadNotifications()
}

function getNotifIcon(type: string): string {
  const map: Record<string, string> = { like: '👍', comment: '💬', follow: '👤', system: '📢' }
  return map[type] || '🔔'
}

function formatTime(d: string): string {
  const diff = Date.now() - new Date(d).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  const days = Math.floor(h / 24)
  if (days < 7) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN')
}

function handleClick(notif: NotificationVO) {
  if (notif.targetId && notif.targetType === 'article') {
    router.push(`/article/${notif.targetId}`)
  } else if (notif.senderId) {
    router.push(`/user/${notif.senderId}`)
  }
}

onMounted(loadNotifications)
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <!-- 头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-lg font-bold text-text-primary">通知</h1>
        <p v-if="unreadCount > 0" class="text-xs text-brand mt-1">{{ unreadCount }} 条未读</p>
      </div>
      <button
        v-if="unreadCount > 0"
        class="text-xs text-brand hover:text-brand-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        :disabled="markingRead"
        @click="handleMarkAllRead"
      >
        {{ markingRead ? '处理中...' : '全部已读' }}
      </button>
    </div>

    <!-- 筛选 -->
    <div class="flex items-center gap-2 mb-6">
      <button
        v-for="f in filters"
        :key="f.key"
        class="px-4 py-1.5 rounded-full text-xs font-medium transition-all duration-200 cursor-pointer"
        :class="typeFilter === f.key
          ? 'bg-brand text-white'
          : 'bg-white text-text-secondary hover:bg-gray-50 border border-gray-100'"
        @click="switchFilter(f.key)"
      >
        {{ f.label }}
      </button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 5" :key="i" class="bg-white rounded-xl p-4 flex gap-3 animate-pulse">
        <div class="w-10 h-10 bg-gray-200 rounded-full flex-shrink-0" />
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-gray-100 rounded w-2/3" />
          <div class="h-3 bg-gray-100 rounded w-full" />
        </div>
      </div>
    </div>

    <!-- 通知列表 -->
    <div v-else class="space-y-2">
      <div
        v-for="notif in notifications"
        :key="notif.id"
        class="bg-white rounded-xl p-4 flex gap-3 cursor-pointer
               shadow-[0_1px_3px_rgba(0,0,0,0.04)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)]
               transition-all duration-300"
        :class="{ 'ring-1 ring-brand/10 bg-brand/[0.02]': !notif.isRead }"
        @click="handleClick(notif)"
      >
        <div class="w-10 h-10 rounded-full flex items-center justify-center text-lg flex-shrink-0"
             :class="notif.isRead ? 'bg-gray-100' : 'bg-brand/10'">
          {{ getNotifIcon(notif.type) }}
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center justify-between gap-2 mb-1">
            <p class="text-sm text-text-primary line-clamp-1" :class="{ 'font-medium': !notif.isRead }">
              {{ notif.title }}
            </p>
            <span class="text-xs text-text-placeholder flex-shrink-0">{{ formatTime(notif.createTime) }}</span>
          </div>
          <p class="text-xs text-text-secondary line-clamp-2">{{ notif.content }}</p>
          <!-- 未读指示点 -->
          <div v-if="!notif.isRead" class="w-2 h-2 rounded-full bg-brand mt-1.5" />
        </div>
      </div>

      <div v-if="notifications.length === 0" class="text-center py-20">
        <svg class="w-16 h-16 text-gray-200 mx-auto mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <p class="text-text-secondary text-sm">暂无通知</p>
      </div>
    </div>
  </div>
</template>
