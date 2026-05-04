<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useToast } from '@/composables/useToast'
import type { ArticleVO } from '@/types'

import { mockArticles, mockArticleRanking, mockAuthorRanking, mockCategories, mockSign } from '@/mock/data'

const toast = useToast()

// ========== 左侧分类 ==========
const categories = mockCategories
const selectedCategoryId = ref(0) // 默认综合

// ========== 中间内容 ==========
const activeTab = ref<'recommend' | 'latest'>('recommend')
const articles = ref<ArticleVO[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const initialLoading = ref(true)
const finished = ref(false)

function selectCategory(id: number) {
  if (selectedCategoryId.value === id) return
  selectedCategoryId.value = id
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
    // 模拟：根据分类和排序筛选
    let list = [...mockArticles.filter(a => a.status === 2)]
    if (selectedCategoryId.value !== 0) {
      // 综合显示全部，其他按分类过滤
      const catMap: Record<number, string> = { 1: '前端', 2: '后端', 3: 'AI', 4: 'Android', 5: 'iOS', 6: '人工智能', 7: '开发工具', 8: '代码人生', 9: '阅读' }
      const catName = catMap[selectedCategoryId.value]
      if (catName) list = list.filter(a => a.categoryName === catName || a.tags?.some(t => t.name === catName))
      if (list.length === 0) list = [mockArticles[0], mockArticles[1]] // fallback
    }
    // 排序
    if (activeTab.value === 'latest') {
      list.sort((a, b) => new Date(b.publishTime).getTime() - new Date(a.publishTime).getTime())
    }
    // 模拟分页
    const start = (page.value - 1) * 8
    const chunk = list.slice(start, start + 8)
    articles.value.push(...chunk)
    total.value = list.length
    if (chunk.length === 0 || articles.value.length >= list.length) finished.value = true
    page.value++
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '加载失败')
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

// ========== 辅助 ==========
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

onMounted(() => {
  loadArticles()
  if (sentinelRef.value) {
    observer = new IntersectionObserver(([e]) => {
      if (e.isIntersecting && !loading.value && !finished.value) loadArticles()
    }, { rootMargin: '300px' })
    observer.observe(sentinelRef.value)
  }
})

onUnmounted(() => observer?.disconnect())

// 骨架屏
const skeletonCount = 6
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <div class="flex gap-6">
      <!-- ============================================================ -->
      <!-- 左侧栏：分类导航（sticky）                                      -->
      <!-- ============================================================ -->
      <aside class="hidden lg:block w-40 flex-shrink-0">
        <nav class="sticky top-20 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-3">
          <template v-for="cat in categories" :key="cat.id">
            <!-- 关注（特殊样式 + 分隔线） -->
            <template v-if="cat.id === 0">
              <button
                class="w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer flex items-center gap-2 mb-1"
                :class="selectedCategoryId === 0 ? 'bg-brand/5 text-brand' : 'text-text-secondary hover:bg-gray-50'"
                @click="selectCategory(0)"
              >
                <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
                关注
              </button>
              <div class="border-t border-gray-50 my-2 mx-3" />
            </template>
            <!-- 其他分类 -->
            <button
              v-else
              class="w-full text-left px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer flex items-center justify-between"
              :class="selectedCategoryId === cat.id ? 'bg-brand/5 text-brand' : 'text-text-secondary hover:bg-gray-50'"
              @click="selectCategory(cat.id)"
            >
              <span>{{ cat.name }}</span>
            </button>
          </template>
        </nav>
      </aside>

      <!-- ============================================================ -->
      <!-- 中间：文章列表                                                  -->
      <!-- ============================================================ -->
      <main class="flex-1 min-w-0">
        <!-- Tab切换 -->
        <div class="flex items-center gap-8 mb-5">
          <button
            v-for="tab in [{ k: 'recommend' as const, l: '推荐' }, { k: 'latest' as const, l: '最新' }]"
            :key="tab.k"
            class="relative pb-2.5 text-sm font-semibold transition-all duration-200 cursor-pointer"
            :class="activeTab === tab.k ? 'text-text-primary' : 'text-text-secondary hover:text-text-primary'"
            @click="switchTab(tab.k)"
          >
            {{ tab.l }}
            <span
              v-if="activeTab === tab.k"
              class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand rounded-full transition-all duration-300"
            />
          </button>
        </div>

        <!-- 骨架屏 -->
        <div v-if="initialLoading" class="space-y-3">
          <div v-for="i in skeletonCount" :key="'sk-'+i" class="bg-white rounded-xl p-4 flex gap-4 animate-pulse">
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
            v-for="item in articles"
            :key="item.id"
            class="bg-white px-5 py-4 cursor-pointer border-b border-gray-50/80
                   hover:bg-gray-50/30 transition-colors duration-150 group"
            @click="$router.push(`/article/${item.id}`)"
          >
            <div class="flex gap-5">
              <!-- 文字区 -->
              <div class="flex-1 min-w-0 flex flex-col justify-between">
                <div>
                  <!-- 标题 -->
                  <h3 class="text-base font-semibold text-text-primary leading-snug line-clamp-2 mb-2
                             group-hover:text-brand transition-colors duration-200">
                    {{ item.title }}
                  </h3>
                  <!-- 摘要 -->
                  <p class="text-sm text-text-secondary/80 leading-relaxed line-clamp-2 mb-3">
                    {{ item.summary }}
                  </p>
                </div>
                <!-- 底部元信息 -->
                <div class="flex items-center gap-4 text-xs text-text-placeholder">
                  <span class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="2"/><path d="M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5z"/></svg>
                    {{ formatCount(item.viewCount) }}
                  </span>
                  <span class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                    {{ formatCount(item.likeCount) }}
                  </span>
                  <span class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    {{ formatCount(item.commentCount) }}
                  </span>
                  <span>{{ item.authorNickname }}</span>
                  <span>{{ timeAgo(item.publishTime) }}</span>
                  <!-- 标签 -->
                  <span v-if="item.tags?.length" class="flex items-center gap-1.5">
                    <span
                      v-for="tag in item.tags.slice(0, 2)"
                      :key="tag.id"
                      class="px-2 py-0.5 rounded-full text-[11px] bg-brand/5 text-brand/80 hover:bg-brand/10 transition-colors"
                    >
                      {{ tag.name }}
                    </span>
                  </span>
                </div>
              </div>
              <!-- 封面图（右侧） -->
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
            <span v-if="loading" class="text-sm text-text-secondary flex items-center gap-2">
              <svg class="animate-spin w-4 h-4" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              加载中...
            </span>
            <span v-else-if="finished && articles.length > 0" class="text-xs text-text-placeholder">— 已经到底了 —</span>
          </div>
        </div>
      </main>

      <!-- ============================================================ -->
      <!-- 右侧栏：签到 + 文章榜 + 作者榜（sticky）                          -->
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
              {{ signed ? '已签到' : '立即签到 +' + mockSign.pointsEarned + '掘力值' }}
            </button>
            <div v-if="signed" class="mt-2 text-center text-xs text-text-placeholder">
              {{ mockSign.bonusDesc }}
            </div>
          </div>

          <!-- 文章榜 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <h4 class="text-sm font-semibold text-text-primary mb-4">🏆 文章榜</h4>
            <div class="space-y-0">
              <div
                v-for="(item, idx) in mockArticleRanking"
                :key="item.id"
                class="flex items-center gap-3 py-2.5 cursor-pointer group border-b border-gray-50/80 last:border-0"
                @click="$router.push(`/article/${item.id}`)"
              >
                <span
                  class="w-5 text-center text-xs font-bold flex-shrink-0"
                  :class="{
                    'text-warm': idx < 3,
                    'text-text-placeholder': idx >= 3,
                  }"
                >{{ idx + 1 }}</span>
                <span class="text-xs text-text-primary line-clamp-1 group-hover:text-brand transition-colors flex-1 min-w-0">
                  {{ item.title }}
                </span>
                <span class="text-[10px] text-text-placeholder flex-shrink-0">{{ formatCount(item.viewCount) }}</span>
              </div>
            </div>
          </div>

          <!-- 作者榜 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <h4 class="text-sm font-semibold text-text-primary mb-4">✍️ 作者榜</h4>
            <div class="space-y-0">
              <div
                v-for="(author, idx) in mockAuthorRanking"
                :key="author.id"
                class="flex items-center gap-3 py-2.5 cursor-pointer group border-b border-gray-50/80 last:border-0"
                @click="$router.push(`/user/${author.id}`)"
              >
                <span
                  class="w-5 text-center text-xs font-bold flex-shrink-0"
                  :class="{ 'text-warm': idx < 3, 'text-text-placeholder': idx >= 3 }"
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
