<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useToast } from '@/composables/useToast'
import { getArticleList } from '@/api/articles'
import { getAllCategories } from '@/api/categories'
import type { ArticleVO, CategoryVO } from '@/types'
import {
  mockArticles, mockArticleRanking, mockAuthorRanking,
  mockCategories as mockCats, mockSign,
} from '@/mock/data'

const toast = useToast()

// ========== 左侧分类 ==========
const categories = ref<CategoryVO[]>([])
const selectedCategoryId = ref(0)
const selectedCategoryName = ref('综合')

/** 分类 icon 名 → SVG 映射 */
const categoryIcons: Record<string, string> = {
  'icon-code':     'M13 2L3 14h6l-2 8 10-12h-6l2-8z',
  'icon-server':   'M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01',
  'icon-mobile':   'M12 18h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z',
  'icon-cpu':      'M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z',
  'icon-tool':     'M14.7 6.3a1 1 0 000-1.4l-1.4-1.4a1 1 0 00-1.4 0l-1.3 1.3 2.8 2.8 1.3-1.3zM4.3 13.3a1 1 0 000 1.4l1.4 1.4a1 1 0 001.4 0l6.6-6.6-2.8-2.8-6.6 6.6z',
  'icon-coffee':   'M18 8h1a4 4 0 010 8h-1M2 8h16v9a4 4 0 01-4 4H6a4 4 0 01-4-4V8zm4-4h8v4H6V4z',
  'icon-book':     'M4 4h16v16H4zm4 0v16M4 8h16M4 16h16',
}

function catIconPath(name: string): string {
  if (categoryIcons[name]) return categoryIcons[name]
  return categoryIcons['icon-code']!
}

// ========== 中间内容 ==========
const activeTab = ref<'recommend' | 'latest'>('recommend')
const articles = ref<ArticleVO[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const initialLoading = ref(true)
const finished = ref(false)

function selectCategory(id: number, name?: string) {
  if (selectedCategoryId.value === id) return
  selectedCategoryId.value = id
  selectedCategoryName.value = name || '综合'
  resetAndLoad()
}

function switchTab(tab: 'recommend' | 'latest') {
  if (activeTab.value === tab) return
  activeTab.value = tab
  resetAndLoad()
}

function resetAndLoad() {
  page.value = 1
  articles.value = []
  finished.value = false
  initialLoading.value = true
  loadArticles()
}

async function loadArticles() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const categoryId = selectedCategoryId.value > 0 ? selectedCategoryId.value : undefined
    const sortBy = activeTab.value === 'latest' ? 'latest' : 'recommend'
    const data = await getArticleList({ page: page.value, size: 8, categoryId, sortBy })
    articles.value.push(...data.list)
    total.value = data.total
    if (data.list.length === 0 || articles.value.length >= data.total) finished.value = true
    page.value++
  } catch {
    let list = [...mockArticles.filter(a => a.status === 2)]
    if (selectedCategoryId.value > 0) {
      const name = selectedCategoryName.value
      list = list.filter(a => a.categoryName === name || a.tags?.some(t => t.name === name))
      if (list.length === 0) list = [mockArticles[0], mockArticles[1]]
    }
    if (activeTab.value === 'latest') list.sort((a, b) => new Date(b.publishTime).getTime() - new Date(a.publishTime).getTime())
    const start = (page.value - 1) * 8
    const chunk = list.slice(start, start + 8)
    articles.value.push(...chunk)
    total.value = list.length
    if (chunk.length === 0 || articles.value.length >= list.length) finished.value = true
    page.value++
  } finally {
    loading.value = false
    initialLoading.value = false
  }
}

// ========== 签到 ==========
const signed = ref(false)
function handleSign() {
  signed.value = true
  toast.success(`签到成功！+${mockSign.pointsEarned}掘力值`)
}

// ========== 辅助函数 ==========
function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function timeAgo(d: string): string {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  const days = Math.floor(h / 24)
  if (days < 30) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// ========== 无限滚动 ==========
let observer: IntersectionObserver | null = null
const sentinelRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  loadArticles()
  try { categories.value = (await getAllCategories()).filter(c => c.id > 0) }
  catch { categories.value = mockCats.filter(c => c.id > 0) }

  if (sentinelRef.value) {
    observer = new IntersectionObserver(([e]) => {
      if (e.isIntersecting && !loading.value && !finished.value) loadArticles()
    }, { rootMargin: '300px' })
    observer.observe(sentinelRef.value)
  }
})

onUnmounted(() => observer?.disconnect())

const skeletonCount = 6
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-4">
    <div class="flex gap-5">
      <!-- ============================================================ -->
      <!-- 左侧栏：分类导航                                                -->
      <!-- ============================================================ -->
      <aside class="hidden lg:block w-44 flex-shrink-0">
        <div class="bg-white rounded-lg border border-gray-200/60 sticky top-20">
          <nav class="p-2">
            <!-- 关注 -->
            <button
              class="w-full text-left px-3 py-2 rounded-md text-sm transition-all duration-200 cursor-pointer flex items-center gap-3"
              :class="selectedCategoryId === -1
                ? 'text-brand bg-blue-50/50 font-medium'
                : 'text-text-secondary hover:bg-gray-50'"
              @click="selectCategory(-1, '关注')"
            >
              <svg class="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span class="truncate">关注</span>
              <span v-if="selectedCategoryId === -1" class="ml-auto w-1.5 h-1.5 rounded-full bg-brand flex-shrink-0" />
            </button>

            <!-- 综合 -->
            <button
              class="w-full text-left px-3 py-2 rounded-md text-sm transition-all duration-200 cursor-pointer flex items-center gap-3"
              :class="selectedCategoryId === 0
                ? 'text-brand bg-blue-50/50 font-medium'
                : 'text-text-secondary hover:bg-gray-50'"
              @click="selectCategory(0, '综合')"
            >
              <svg class="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
              </svg>
              <span class="truncate">综合</span>
              <span v-if="selectedCategoryId === 0" class="ml-auto w-1.5 h-1.5 rounded-full bg-brand flex-shrink-0" />
            </button>

            <!-- 分隔线 -->
            <div class="border-t border-gray-100 my-2 mx-2" />

            <!-- API 分类列表 -->
            <button
              v-for="cat in categories"
              :key="cat.id"
              class="w-full text-left px-3 py-2 rounded-md text-sm transition-all duration-200 cursor-pointer flex items-center gap-3"
              :class="selectedCategoryId === cat.id
                ? 'text-brand bg-blue-50/50 font-medium'
                : 'text-text-secondary hover:bg-gray-50'"
              @click="selectCategory(cat.id, cat.name)"
            >
              <svg class="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path :d="catIconPath(cat.icon)" />
              </svg>
              <span class="truncate">{{ cat.name }}</span>
              <span v-if="selectedCategoryId === cat.id" class="ml-auto w-1.5 h-1.5 rounded-full bg-brand flex-shrink-0" />
            </button>
          </nav>
        </div>
      </aside>

      <!-- ============================================================ -->
      <!-- 中间：文章列表                                                  -->
      <!-- ============================================================ -->
      <main class="flex-1 min-w-0">
        <!-- Tab 切换 -->
        <div class="flex items-center gap-8 mb-4">
          <button
            v-for="tab in [
              { k: 'recommend' as const, l: '推荐' },
              { k: 'latest' as const, l: '最新' },
            ]"
            :key="tab.k"
            class="relative pb-2.5 text-sm font-semibold transition-all duration-200 cursor-pointer"
            :class="activeTab === tab.k ? 'text-text-primary' : 'text-text-secondary hover:text-text-primary'"
            @click="switchTab(tab.k)"
          >
            {{ tab.l }}
            <span
              v-if="activeTab === tab.k"
              class="absolute bottom-0 left-1/2 -translate-x-1/2 w-5 h-0.5 bg-brand rounded-full"
            />
          </button>
        </div>

        <!-- 骨架屏 -->
        <div v-if="initialLoading" class="space-y-0">
          <div
            v-for="i in skeletonCount" :key="'sk-'+i"
            class="bg-white px-5 py-4 border-b border-gray-50/80 flex gap-5 animate-pulse"
          >
            <div class="flex-1 space-y-3">
              <div class="h-5 bg-gray-100 rounded w-3/4" />
              <div class="h-4 bg-gray-100 rounded w-full" />
              <div class="h-3 bg-gray-100 rounded w-1/2" />
            </div>
            <div class="w-28 h-20 bg-gray-200 rounded-lg flex-shrink-0" />
          </div>
        </div>

        <!-- 文章列表 -->
        <div v-else class="space-y-0">
          <article
            v-for="(item, idx) in articles"
            :key="item.id"
            class="bg-white px-5 py-4 cursor-pointer border-b border-gray-50/80
                   hover:bg-gray-50/30 transition-colors duration-150 group"
            :class="{ 'border-t border-gray-100/60': idx === 0 }"
            @click="$router.push(`/article/${item.id}`)"
          >
            <div class="flex gap-5">
              <!-- 文字区 -->
              <div class="flex-1 min-w-0 flex flex-col justify-between">
                <div>
                  <!-- 标题 -->
                  <h3 class="text-base font-semibold text-text-primary leading-snug mb-1.5
                             group-hover:text-brand transition-colors duration-200"
                      :class="activeTab === 'recommend' ? 'line-clamp-2' : 'line-clamp-1'">
                    {{ item.title }}
                  </h3>
                  <!-- 摘要 -->
                  <p
                    class="text-sm text-text-secondary/75 leading-relaxed mb-2"
                    :class="activeTab === 'recommend' ? 'line-clamp-2' : 'line-clamp-1'"
                  >
                    {{ item.summary }}
                  </p>
                </div>
                <!-- 底部元信息 -->
                <div class="flex items-center gap-3 text-xs text-text-placeholder flex-wrap">
                  <span>{{ item.authorNickname }}</span>
                  <span class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                    {{ formatCount(item.likeCount) }}
                  </span>
                  <span class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    {{ formatCount(item.commentCount) }}
                  </span>
                  <!-- 最新 tab 特有：相对时间 -->
                  <span v-if="activeTab === 'latest' && item.publishTime" class="text-text-placeholder/70">
                    {{ timeAgo(item.publishTime) }}
                  </span>
                  <!-- 标签 -->
                  <span v-if="item.tags?.length" class="flex items-center gap-1.5">
                    <span
                      v-for="tag in item.tags.slice(0, 2)"
                      :key="tag.id"
                      class="px-2 py-0.5 rounded-full text-[11px] bg-brand/5 text-brand/80 hover:bg-brand/10 transition-colors cursor-pointer"
                      @click.stop="$router.push(`/categories/${tag.id}`)"
                    >
                      {{ tag.name }}
                    </span>
                  </span>
                </div>
              </div>
              <!-- 封面图 -->
              <img
                v-if="item.coverImage"
                :src="item.coverImage"
                :alt="item.title"
                class="w-28 h-20 rounded-lg object-cover flex-shrink-0
                       group-hover:opacity-90 transition-opacity duration-300"
                loading="lazy"
              />
            </div>
          </article>

          <!-- 加载指示器 -->
          <div ref="sentinelRef" class="flex justify-center py-8">
            <template v-if="loading">
              <svg class="animate-spin w-5 h-5 text-brand" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
            </template>
            <span v-else-if="finished && articles.length > 0" class="text-xs text-text-placeholder">— 已经到底了 —</span>
          </div>
        </div>
      </main>

      <!-- ============================================================ -->
      <!-- 右侧栏：签到 + 文章榜 + 作者榜                                     -->
      <!-- ============================================================ -->
      <aside class="hidden xl:block w-64 flex-shrink-0">
        <div class="sticky top-20 space-y-4">

          <!-- 签到卡片 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <div class="flex items-center justify-between mb-3">
              <h4 class="text-sm font-semibold text-text-primary">每日签到</h4>
              <span class="text-xs text-text-placeholder">连续 {{ mockSign.continuousDays }} 天</span>
            </div>
            <button
              :disabled="signed"
              class="w-full h-10 rounded-xl text-sm font-semibold transition-all duration-200 cursor-pointer
                     flex items-center justify-center gap-2"
              :class="signed
                ? 'bg-green/10 text-green cursor-default'
                : 'bg-gradient-to-r from-brand to-brand-light text-white hover:shadow-lg hover:shadow-brand/20'"
              @click="handleSign"
            >
              <svg v-if="signed" class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6L9 17l-5-5"/></svg>
              {{ signed ? '已签到' : `立即签到 +${mockSign.pointsEarned}掘力值` }}
            </button>
            <div v-if="signed" class="mt-2 text-center text-xs text-text-placeholder">{{ mockSign.bonusDesc }}</div>
          </div>

          <!-- 文章榜 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <h4 class="text-sm font-semibold text-text-primary mb-4">文章榜</h4>
            <div class="space-y-0">
              <div
                v-for="(entry, idx) in mockArticleRanking"
                :key="entry.id"
                class="flex items-center gap-3 py-2.5 cursor-pointer group border-b border-gray-50/80 last:border-0"
                @click="$router.push(`/article/${entry.id}`)"
              >
                <span
                  class="w-5 text-center text-xs font-bold flex-shrink-0"
                  :class="idx < 3 ? 'text-warm' : 'text-text-placeholder'"
                >{{ idx + 1 }}</span>
                <span class="text-xs text-text-primary line-clamp-1 group-hover:text-brand transition-colors flex-1 min-w-0">{{ entry.title }}</span>
                <span class="text-[10px] text-text-placeholder flex-shrink-0">{{ formatCount(entry.viewCount) }}</span>
              </div>
            </div>
          </div>

          <!-- 作者榜 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <h4 class="text-sm font-semibold text-text-primary mb-4">作者榜</h4>
            <div class="space-y-0">
              <div
                v-for="(author, idx) in mockAuthorRanking"
                :key="author.id"
                class="flex items-center gap-3 py-2.5 cursor-pointer group border-b border-gray-50/80 last:border-0"
                @click="$router.push(`/user/${author.id}`)"
              >
                <span
                  class="w-5 text-center text-xs font-bold flex-shrink-0"
                  :class="idx < 3 ? 'text-warm' : 'text-text-placeholder'"
                >{{ idx + 1 }}</span>
                <div class="w-7 h-7 rounded-full bg-gradient-to-br from-brand/20 to-brand-light/30 flex items-center justify-center text-[10px] font-bold text-brand flex-shrink-0">
                  {{ author.nickname?.charAt(0) }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-xs text-text-primary font-medium line-clamp-1 group-hover:text-brand transition-colors">{{ author.nickname }}</p>
                  <p class="text-[10px] text-text-placeholder">Lv.{{ author.level }} · {{ author.articleCount }}篇</p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </aside>
    </div>
  </div>
</template>
