<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAllTags } from '@/api/tags'
import type { TagVO } from '@/types'

const tags = ref<TagVO[]>([])
const loading = ref(true)
const viewMode = ref<'grid' | 'list'>('grid')

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

onMounted(async () => {
  try {
    tags.value = await getAllTags()
  } catch (e: unknown) {
    tags.value = []
    // 加载失败不影响UI，只记录
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-lg font-bold text-text-primary">标签</h1>
        <p class="text-sm text-text-secondary mt-1">探索你感兴趣的技术话题</p>
      </div>
      <div class="flex items-center gap-1 bg-gray-100 rounded-lg p-0.5">
        <button
          class="px-3 py-1 text-xs rounded-md transition-all cursor-pointer"
          :class="viewMode === 'grid' ? 'bg-white text-text-primary font-medium shadow-sm' : 'text-text-secondary'"
          @click="viewMode = 'grid'"
        >
          网格
        </button>
        <button
          class="px-3 py-1 text-xs rounded-md transition-all cursor-pointer"
          :class="viewMode === 'list' ? 'bg-white text-text-primary font-medium shadow-sm' : 'text-text-secondary'"
          @click="viewMode = 'list'"
        >
          列表
        </button>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="grid grid-cols-2 md:grid-cols-3 gap-4">
      <div v-for="i in 9" :key="i" class="bg-white rounded-xl p-5 animate-pulse">
        <div class="h-4 bg-gray-100 rounded w-2/3 mb-3" />
        <div class="h-3 bg-gray-100 rounded w-full mb-2" />
        <div class="h-3 bg-gray-100 rounded w-1/2" />
      </div>
    </div>

    <!-- 网格视图 -->
    <div v-else-if="viewMode === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="bg-white rounded-xl p-5 cursor-pointer
               shadow-[0_1px_3px_rgba(0,0,0,0.04)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)]
               hover:-translate-y-0.5 transition-all duration-300"
        @click="$router.push(`/search?tagId=${tag.id}`)"
      >
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand/10 to-brand-light/20
                    flex items-center justify-center mb-3">
          <span class="text-sm font-bold text-brand">{{ tag.name?.charAt(0) }}</span>
        </div>
        <h3 class="text-sm font-semibold text-text-primary mb-1">{{ tag.name }}</h3>
        <p v-if="tag.description" class="text-xs text-text-secondary line-clamp-2 mb-3">{{ tag.description }}</p>
        <div class="flex items-center gap-3 text-xs text-text-placeholder">
          <span>{{ formatCount(tag.articleCount) }} 文章</span>
          <span>{{ formatCount(tag.followCount) }} 关注</span>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="space-y-3">
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="bg-white rounded-xl p-4 flex items-center gap-4 cursor-pointer
               shadow-[0_1px_3px_rgba(0,0,0,0.04)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)]
               transition-all duration-300"
        @click="$router.push(`/search?tagId=${tag.id}`)"
      >
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand/10 to-brand-light/20 flex items-center justify-center flex-shrink-0">
          <span class="text-sm font-bold text-brand">{{ tag.name?.charAt(0) }}</span>
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="text-sm font-semibold text-text-primary">{{ tag.name }}</h3>
          <p v-if="tag.description" class="text-xs text-text-secondary line-clamp-1">{{ tag.description }}</p>
        </div>
        <div class="flex items-center gap-4 text-xs text-text-placeholder flex-shrink-0">
          <span>{{ formatCount(tag.articleCount) }} 文章</span>
          <span>{{ formatCount(tag.followCount) }} 关注</span>
        </div>
      </div>
    </div>

    <div v-if="!loading && tags.length === 0" class="text-center py-20">
      <p class="text-text-secondary text-sm">暂无标签</p>
    </div>
  </div>
</template>
