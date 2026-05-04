<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '@/api/articles'
import type { ArticleVO } from '@/types'

const route = useRoute()
const router = useRouter()

const keyword = ref((route.query.q as string) || (route.query.keyword as string) || '')
const searchInput = ref(keyword.value)
const results = ref<ArticleVO[]>([])
const loading = ref(false)
const searched = ref(false)
const page = ref(1)
const total = ref(0)

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function timeAgo(d: string): string {
  const diff = Date.now() - new Date(d).getTime()
  const h = Math.floor(diff / 3600000)
  if (h < 24) return `${h}小时前`
  const days = Math.floor(h / 24)
  if (days < 30) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN')
}

// 高亮关键词
function highlight(text: string): string {
  if (!keyword.value) return text
  const re = new RegExp(`(${keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(re, '<mark class="bg-warm/20 text-warm rounded px-0.5">$1</mark>')
}

async function doSearch() {
  if (!searchInput.value.trim()) return
  keyword.value = searchInput.value.trim()
  loading.value = true
  searched.value = true
  page.value = 1
  results.value = []
  router.replace({ query: { q: keyword.value } })
  try {
    const data = await searchArticles({ keyword: keyword.value, page: 1, size: 20 })
    results.value = data.list
    total.value = data.total
  } catch (e: unknown) {
    results.value = []
    total.value = 0
  } finally { loading.value = false }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') doSearch()
}

onMounted(() => {
  if (keyword.value) doSearch()
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <!-- 搜索框 -->
    <div class="mb-8">
      <div class="flex items-center gap-3">
        <div class="flex-1 relative">
          <svg class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-placeholder" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          <input
            v-model="searchInput"
            type="text"
            placeholder="搜索文章..."
            class="w-full h-12 pl-12 pr-4 rounded-2xl bg-white border border-gray-100 text-sm outline-none
                   shadow-[0_1px_3px_rgba(0,0,0,0.04)] focus:border-brand/30 focus:ring-2 focus:ring-brand/5
                   transition-all duration-200 placeholder:text-text-placeholder"
            @keydown="handleKeydown"
          />
        </div>
        <button
          class="h-12 px-6 bg-brand text-white text-sm font-medium rounded-2xl hover:bg-brand-dark transition-all duration-200 cursor-pointer"
          :disabled="loading"
          @click="doSearch"
        >
          搜索
        </button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searched">
      <p class="text-sm text-text-secondary mb-4">
        {{ loading ? '搜索中...' : `找到 ${total} 条关于「${keyword}」的结果` }}
      </p>

      <div v-if="loading" class="space-y-4">
        <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-4 flex gap-4 animate-pulse">
          <div class="w-28 h-20 bg-gray-200 rounded-lg flex-shrink-0" />
          <div class="flex-1 space-y-2">
            <div class="h-4 bg-gray-100 rounded w-3/4" />
            <div class="h-3 bg-gray-100 rounded w-full" />
            <div class="h-3 bg-gray-100 rounded w-1/2" />
          </div>
        </div>
      </div>

      <div v-else-if="results.length > 0" class="space-y-3">
        <article
          v-for="article in results"
          :key="article.id"
          class="bg-white rounded-xl p-4 flex gap-4 cursor-pointer
                 shadow-[0_1px_3px_rgba(0,0,0,0.04)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)]
                 hover:-translate-y-0.5 transition-all duration-300"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div v-if="article.coverImage" class="w-28 h-20 rounded-lg overflow-hidden flex-shrink-0">
            <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" loading="lazy" />
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-sm font-semibold text-text-primary line-clamp-2 mb-1.5 hover:text-brand transition-colors" v-html="highlight(article.title)" />
            <p class="text-xs text-text-secondary line-clamp-1 mb-2" v-html="highlight(article.summary || '')" />
            <div class="flex items-center gap-4 text-xs text-text-secondary/70">
              <span>{{ article.authorNickname }}</span>
              <span class="flex items-center gap-0.5">
                <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="2"/><path d="M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5z"/></svg>
                {{ formatCount(article.viewCount) }}
              </span>
              <span>{{ timeAgo(article.publishTime || article.createTime) }}</span>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="text-center py-20">
        <svg class="w-16 h-16 text-gray-200 mx-auto mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
        <p class="text-text-secondary text-sm">没有找到相关内容</p>
        <p class="text-text-placeholder text-xs mt-1">试试更换关键词</p>
      </div>
    </div>

    <!-- 未搜索时的提示 -->
    <div v-else class="text-center py-20">
      <svg class="w-16 h-16 text-gray-200 mx-auto mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
      </svg>
      <p class="text-text-secondary text-sm">输入关键词搜索感兴趣的文章</p>
    </div>
  </div>
</template>
