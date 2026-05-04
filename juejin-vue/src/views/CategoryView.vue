<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategoryById } from '@/api/categories'
import { getArticleList } from '@/api/articles'
import type { CategoryVO, ArticleVO } from '@/types'

const route = useRoute()
const categoryId = computed(() => Number(route.params.id))

const category = ref<CategoryVO | null>(null)
const articles = ref<ArticleVO[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(true)
const listLoading = ref(false)
const loadError = ref('')

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

async function loadCategory() {
  loading.value = true
  try {
    category.value = await getCategoryById(categoryId.value)
    await loadArticles()
  } catch (e: unknown) {
    loadError.value = e instanceof Error ? e.message : '加载失败'
  } finally { loading.value = false }
}

async function loadArticles() {
  listLoading.value = true
  try {
    const data = await getArticleList({ page: page.value, size: 10, categoryId: categoryId.value })
    articles.value = data.list
    total.value = data.total
  } catch (e: unknown) {
    loadError.value = e instanceof Error ? e.message : '加载文章失败'
    articles.value = []
  } finally { listLoading.value = false }
}

import { computed } from 'vue'

watch(() => route.params.id, () => {
  page.value = 1
  loadCategory()
})

onMounted(loadCategory)
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <!-- 加载 -->
    <div v-if="loading" class="flex justify-center py-20">
      <svg class="animate-spin w-8 h-8 text-brand" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <div v-if="loadError" class="mb-4 bg-red-50 text-red-600 text-sm rounded-xl px-4 py-3 text-center">{{ loadError }}</div>

    <template v-else-if="category">
      <!-- 分类头部 -->
      <div class="bg-white rounded-2xl p-8 mb-6 shadow-[0_1px_3px_rgba(0,0,0,0.04)]
                  bg-gradient-to-br from-brand/5 via-white to-brand-light/5">
        <h1 class="text-2xl font-bold text-text-primary mb-2">{{ category.name }}</h1>
        <p v-if="category.description" class="text-sm text-text-secondary mb-3">{{ category.description }}</p>
        <span class="text-xs text-text-placeholder">{{ category.articleCount }} 篇文章</span>
      </div>

      <!-- 文章列表 -->
      <div class="space-y-3">
        <article
          v-for="article in articles"
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
            <h3 class="text-sm font-semibold text-text-primary line-clamp-2 mb-1.5 hover:text-brand transition-colors">
              {{ article.title }}
            </h3>
            <p class="text-xs text-text-secondary line-clamp-1 mb-2">{{ article.summary }}</p>
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

      <div v-if="!listLoading && articles.length === 0" class="text-center py-12">
        <p class="text-sm text-text-placeholder">该分类下暂无文章</p>
      </div>
    </template>
  </div>
</template>
