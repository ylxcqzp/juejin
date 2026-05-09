<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDraftList, deleteDraftArticle as deleteDraft } from '@/api/articles'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { ArticleVO } from '@/types'

const router = useRouter()
const toast = useToast()

const drafts = ref<ArticleVO[]>([])
const loading = ref(true)
const total = ref(0)
const { isSubmitting: deleting, withLock: withDeleteLock } = useSubmitLock()

function formatDate(d: string): string {
  return new Date(d).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function loadDrafts() {
  loading.value = true
  try {
    const data = await getDraftList({ page: 1, size: 50 })
    drafts.value = data.list
    total.value = data.total
  } catch (e: unknown) {
    drafts.value = []
    toast.error(e instanceof Error ? e.message : '草稿加载失败')
  } finally { loading.value = false }
}

async function handleDelete(draftId: number) {
  if (!window.confirm('确定删除这篇草稿？')) return
  await withDeleteLock(async () => {
    await deleteDraft(draftId)
    drafts.value = drafts.value.filter(d => d.id !== draftId)
  })
}

function handleEdit(draft: ArticleVO) {
  router.push(`/editor/${draft.id}`)
}

onMounted(loadDrafts)
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-lg font-bold text-text-primary">草稿箱</h1>
        <p class="text-xs text-text-secondary mt-1">共 {{ total }} 篇草稿</p>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-4 flex gap-4 animate-pulse">
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-gray-100 rounded w-1/2" />
          <div class="h-3 bg-gray-100 rounded w-full" />
          <div class="h-3 bg-gray-100 rounded w-1/3" />
        </div>
      </div>
    </div>

    <!-- 草稿列表 -->
    <div v-else class="space-y-3">
      <div
        v-for="draft in drafts"
        :key="draft.id"
        class="bg-white rounded-xl p-4 shadow-[0_1px_3px_rgba(0,0,0,0.04)]
               hover:shadow-[0_4px_16px_rgba(0,0,0,0.06)] transition-all duration-300"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <h3 class="text-sm font-semibold text-text-primary line-clamp-1 mb-1">
              {{ draft.title || '未命名草稿' }}
            </h3>
            <p class="text-xs text-text-secondary line-clamp-2 mb-2">
              {{ draft.content || '暂无内容' }}
            </p>
            <span class="text-xs text-text-placeholder">{{ formatDate(draft.createTime) }}</span>
          </div>
          <div class="flex items-center gap-2 flex-shrink-0">
            <button
              class="px-3 py-1.5 text-xs font-medium rounded-lg bg-brand/5 text-brand hover:bg-brand/10 transition-colors cursor-pointer"
              @click="handleEdit(draft)"
            >
              继续编辑
            </button>
            <button
              class="px-3 py-1.5 text-xs text-text-secondary rounded-lg hover:bg-red-50 hover:text-red-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="deleting"
              @click="handleDelete(draft.id)"
            >
              {{ deleting ? '删除中...' : '删除' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="drafts.length === 0" class="text-center py-20">
        <svg class="w-16 h-16 text-gray-200 mx-auto mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
        </svg>
        <p class="text-text-secondary text-sm">暂无草稿</p>
        <button class="text-brand text-sm mt-2 hover:underline cursor-pointer" @click="router.push('/editor')">去写一篇文章</button>
      </div>
    </div>
  </div>
</template>
