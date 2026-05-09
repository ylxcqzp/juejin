<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getPinList, getHotTopics, createPin } from '@/api/pins'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { PinVO, PinTopicVO } from '@/types'

const authStore = useAuthStore()
const toast = useToast()

const pins = ref<PinVO[]>([])
const topics = ref<PinTopicVO[]>([])
const page = ref(1)
const loading = ref(true)
const finished = ref(false)
const sortBy = ref<'latest' | 'hot'>('latest')

// 发布沸点
const showComposer = ref(false)
const newPinContent = ref('')
const { isSubmitting: publishing, withLock: withPublishLock } = useSubmitLock()

async function loadPins(reset = false) {
  if (loading.value && !reset) return
  if (reset) { page.value = 1; pins.value = [] }
  loading.value = true
  try {
    const data = await getPinList({ page: page.value, size: 15, sortBy: sortBy.value })
    if (reset) pins.value = data.list
    else pins.value.push(...data.list)
    finished.value = data.list.length === 0 || pins.value.length >= data.total
    if (!finished.value) page.value++
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '加载失败')
    finished.value = true
  }
  finally { loading.value = false }
}

async function loadTopics() {
  try { topics.value = await getHotTopics(10) } catch { /* 话题非关键功能 */ }
}

function switchSort(s: 'latest' | 'hot') {
  if (s === sortBy.value) return
  sortBy.value = s
  loadPins(true)
}

async function handlePublish() {
  if (!newPinContent.value.trim()) return
  await withPublishLock(async () => {
    try {
      await createPin({ content: newPinContent.value })
      newPinContent.value = ''
      showComposer.value = false
      loadPins(true)
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '发布失败')
    }
  })
}

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function timeAgo(d: string): string {
  const diff = Date.now() - new Date(d).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  const days = Math.floor(h / 24)
  if (days < 30) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN')
}

let observer: IntersectionObserver | null = null

onMounted(() => {
  loadPins(true)
  loadTopics()
  const sentinel = document.getElementById('pin-sentinel')
  if (sentinel) {
    observer = new IntersectionObserver(
      ([e]) => { if (e.isIntersecting && !loading.value && !finished.value) loadPins() },
      { rootMargin: '200px' },
    )
    observer.observe(sentinel)
  }
})

onUnmounted(() => observer?.disconnect())
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-6">
    <div class="flex gap-8">
      <!-- 主内容 -->
      <div class="flex-1 min-w-0">
        <!-- 顶部栏 -->
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-6">
            <h1 class="text-lg font-bold text-text-primary">沸点</h1>
            <div class="flex items-center gap-1 bg-gray-100 rounded-lg p-0.5">
              <button
                v-for="s in [{ k: 'latest' as const, l: '最新' }, { k: 'hot' as const, l: '热门' }]"
                :key="s.k"
                class="px-3 py-1 text-xs rounded-md transition-all cursor-pointer"
                :class="sortBy === s.k ? 'bg-white text-text-primary font-medium shadow-sm' : 'text-text-secondary'"
                @click="switchSort(s.k)"
              >
                {{ s.l }}
              </button>
            </div>
          </div>
          <button
            v-if="authStore.isLoggedIn"
            class="px-4 py-2 bg-brand text-white text-xs font-medium rounded-full
                   hover:bg-brand-dark transition-all duration-200 cursor-pointer"
            @click="showComposer = !showComposer"
          >
            + 发布沸点
          </button>
        </div>

        <!-- 发布沸点 -->
        <Transition name="fade">
          <div v-if="showComposer" class="bg-white rounded-xl p-4 shadow-sm mb-4">
            <textarea
              v-model="newPinContent"
              placeholder="分享你的想法..."
              maxlength="500"
              rows="3"
              class="w-full text-sm outline-none resize-none placeholder:text-text-placeholder/60"
            />
            <div class="flex items-center justify-between mt-2">
              <span class="text-xs text-text-placeholder">{{ newPinContent.length }}/1000</span>
              <div class="flex gap-2">
                <button class="px-4 py-1.5 text-xs text-text-secondary rounded-full bg-gray-100 hover:bg-gray-200 transition-colors cursor-pointer" @click="showComposer = false">取消</button>
                <button
                  :disabled="!newPinContent.trim() || publishing"
                  class="px-4 py-1.5 bg-brand text-white text-xs font-medium rounded-full hover:bg-brand-dark transition-all disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
                  @click="handlePublish"
                >
                  发布
                </button>
              </div>
            </div>
          </div>
        </Transition>

        <!-- 沸点列表 -->
        <div class="space-y-3">
          <div
            v-for="pin in pins"
            :key="pin.id"
            class="bg-white rounded-xl p-5 shadow-[0_1px_3px_rgba(0,0,0,0.04)]
                   hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)] transition-all duration-300"
          >
            <div class="flex gap-3">
              <div
                class="w-9 h-9 rounded-full bg-gradient-to-br from-brand/20 to-brand-light/30
                       flex items-center justify-center text-xs font-bold text-brand flex-shrink-0
                       cursor-pointer"
                @click="$router.push(`/user/${pin.userId}`)"
              >
                {{ pin.userNickname?.charAt(0) || 'U' }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="text-sm font-semibold text-text-primary cursor-pointer hover:text-brand" @click="$router.push(`/user/${pin.userId}`)">
                    {{ pin.userNickname }}
                  </span>
                  <span v-if="pin.topicName" class="text-xs text-brand bg-brand/5 px-1.5 py-0.5 rounded-full">{{ pin.topicName }}</span>
                </div>
                <p class="text-sm text-text-primary leading-relaxed mb-3">{{ pin.content }}</p>
                <!-- 图片 -->
                <div v-if="pin.images?.length" class="grid gap-2 mb-3" :class="pin.images.length === 1 ? 'grid-cols-1' : 'grid-cols-3'">
                  <img
                    v-for="(img, i) in pin.images.slice(0, 9)"
                    :key="i"
                    :src="img"
                    class="w-full rounded-lg object-cover cursor-pointer hover:opacity-90 transition-opacity"
                    :style="{ aspectRatio: '1/1' }"
                    loading="lazy"
                  />
                </div>
                <div class="flex items-center gap-5 text-xs text-text-secondary">
                  <span>{{ timeAgo(pin.createTime) }}</span>
                  <span class="flex items-center gap-1 cursor-pointer hover:text-brand transition-colors">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                    {{ formatCount(pin.likeCount) }}
                  </span>
                  <span class="flex items-center gap-1 cursor-pointer hover:text-brand transition-colors">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    {{ formatCount(pin.commentCount) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div id="pin-sentinel" class="flex justify-center py-8">
          <span v-if="loading" class="text-sm text-text-secondary">加载中...</span>
          <span v-else-if="finished && pins.length > 0" class="text-xs text-text-placeholder">— 已经到底了 —</span>
        </div>
      </div>

      <!-- 右侧话题栏 -->
      <aside class="hidden lg:block w-60 flex-shrink-0">
        <div class="sticky top-20 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
          <h3 class="text-sm font-semibold text-text-primary mb-4">热门话题</h3>
          <div class="space-y-3">
            <div
              v-for="topic in topics"
              :key="topic.id"
              class="cursor-pointer group"
              @click="$router.push(`/pins?topicId=${topic.id}`)"
            >
              <div class="flex items-center gap-2">
                <span class="text-sm text-text-primary group-hover:text-brand transition-colors truncate">{{ topic.name }}</span>
                <span v-if="topic.isHot" class="text-[10px] text-warm bg-warm/10 px-1 rounded">热</span>
              </div>
              <p class="text-xs text-text-placeholder mt-0.5">{{ topic.pinCount }} 条沸点</p>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
