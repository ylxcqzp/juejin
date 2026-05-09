<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { createArticle, createDraftArticle, updateDraftArticle, getArticle, type ArticleCreateDTO } from '@/api/articles'
import { getAllCategories } from '@/api/categories'
import { getAllTags } from '@/api/tags'
import { uploadImage } from '@/api/uploads'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import { marked } from '@/utils/marked-setup'
import type { CategoryVO, TagVO } from '@/types'

const router = useRouter()
const route = useRoute()
const toast = useToast()

// 编辑模式：/editor/:id 时加载已有文章
const articleId = route.params.id ? Number(route.params.id) : null
const isEditMode = ref(!!articleId)

// 表单状态
const title = ref('')
const content = ref('')
const coverImage = ref('')
const summary = ref('')
const selectedCategoryId = ref<number | undefined>()
const selectedTagIds = ref<number[]>([])
const isOriginal = ref(1)  // 0=转载 1=原创
const { isSubmitting: publishing, withLock: withPublishLock } = useSubmitLock()
const { isSubmitting: saving, withLock: withSaveLock } = useSubmitLock()
const publishSuccess = ref(false)

// 表单校验
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

const validationErrors = ref<string[]>([])
function validateForm(): boolean {
  const errors: string[] = []
  if (!title.value.trim()) errors.push('标题不能为空')
  if (!content.value.trim()) errors.push('内容不能为空')
  if (selectedCategoryId.value == null) errors.push('请选择分类')
  if (content.value.length > 50000) errors.push('内容不能超过50000字')
  validationErrors.value = errors
  return errors.length === 0
}

// 预览
const previewHtml = computed(() => marked.parse(content.value || '') as string)

// 分类和标签数据
const categories = ref<CategoryVO[]>([])
const tags = ref<TagVO[]>([])

// 字符计数
const charCount = computed(() => content.value.length)

// 脏状态（未保存变更）
const isDirty = ref(false)
watch([title, content], () => { isDirty.value = true })

// 自动保存
let autoSaveTimer: ReturnType<typeof setTimeout> | null = null
const autoSaveStatus = ref('') // '' | 'saving' | 'saved'

function triggerAutoSave() {
  if (!title.value && !content.value) return
  autoSaveStatus.value = 'saving'
  clearTimeout(autoSaveTimer!)
  autoSaveTimer = setTimeout(async () => {
    try {
      // 临时草稿存储到 localStorage
      const draft = { title: title.value, content: content.value, coverImage: coverImage.value, timestamp: Date.now() }
      localStorage.setItem('editor_draft', JSON.stringify(draft))
      autoSaveStatus.value = 'saved'
      isDirty.value = false
      setTimeout(() => { if (autoSaveStatus.value === 'saved') autoSaveStatus.value = '' }, 2000)
    } catch { autoSaveStatus.value = '' }
  }, 2000)
}

watch([title, content, coverImage], () => {
  if (isDirty.value) triggerAutoSave()
})

// 加载分类/标签，编辑模式下加载文章
onMounted(async () => {
  try {
    const [cats, tgs] = await Promise.all([getAllCategories(), getAllTags()])
    categories.value = cats
    tags.value = tgs
  } catch {
    toast.error('分类/标签加载失败，请刷新页面重试')
  }

  // 编辑模式：加载已有文章内容
  if (articleId) {
    try {
      const article = await getArticle(articleId)
      title.value = article.title || ''
      content.value = article.content || ''
      coverImage.value = article.coverImage || ''
      selectedCategoryId.value = article.categoryId
      if (article.tags) selectedTagIds.value = article.tags.map(t => t.id)
      isOriginal.value = article.isOriginal ? 1 : 0
      isEditMode.value = true
    } catch {
      toast.error('文章加载失败')
      router.push('/editor')
    }
    return
  }

  // 新建模式：恢复本地草稿
  const saved = localStorage.getItem('editor_draft')
  if (saved) {
    try {
      const draft = JSON.parse(saved)
      if (draft.timestamp && Date.now() - draft.timestamp < 24 * 60 * 60 * 1000) {
        title.value = draft.title || ''
        content.value = draft.content || ''
        coverImage.value = draft.coverImage || ''
      }
    } catch { /* ignore */ }
  }
})

onUnmounted(() => clearTimeout(autoSaveTimer!))

// 离开确认
onBeforeRouteLeave((_to, _from, next) => {
  if (isDirty.value && (title.value || content.value)) {
    if (window.confirm('你有未保存的内容，确定要离开吗？')) {
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

function toggleTag(tagId: number) {
  const idx = selectedTagIds.value.indexOf(tagId)
  if (idx >= 0) selectedTagIds.value.splice(idx, 1)
  else if (selectedTagIds.value.length < 5) selectedTagIds.value.push(tagId)
}

// 在光标位置插入文本
function insertAtCursor(text: string) {
  const textarea = textareaRef.value
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = content.value.substring(0, start)
  const after = content.value.substring(end)
  content.value = before + text + after
  nextTick(() => {
    textarea.selectionStart = textarea.selectionEnd = start + text.length
    textarea.focus()
  })
}

// 上传图片并插入 Markdown
async function uploadAndInsertImage(file: File) {
  if (file.size > 10 * 1024 * 1024) {
    toast.error('图片不能超过10MB')
    return
  }
  uploading.value = true
  try {
    const url = await uploadImage(file)
    const markdown = `![${file.name}](${url})`
    insertAtCursor(markdown)
    toast.success('图片已插入')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '图片上传失败'
    toast.error(msg)
  } finally {
    uploading.value = false
  }
}

// 粘贴图片
function handlePaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of Array.from(items)) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (file) uploadAndInsertImage(file)
      break
    }
  }
}

// 拖拽图片
function handleDrop(e: DragEvent) {
  e.preventDefault()
  const files = e.dataTransfer?.files
  if (!files) return
  for (const file of Array.from(files)) {
    if (file.type.startsWith('image/')) {
      uploadAndInsertImage(file)
    }
  }
}

// 文件选择器
function triggerImagePicker() {
  fileInputRef.value?.click()
}

function handleFileInputChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files) return
  for (const file of Array.from(input.files)) {
    if (file.type.startsWith('image/')) {
      uploadAndInsertImage(file)
    }
  }
  input.value = '' // 允许重复选择同一文件
}

// 发布文章
async function handlePublish() {
  if (!validateForm()) {
    validationErrors.value.forEach(e => toast.error(e))
    return
  }
  await withPublishLock(async () => {
    try {
      const articleData: ArticleCreateDTO = {
        title: title.value,
        content: content.value,
        contentHtml: previewHtml.value,
        summary: summary.value || content.value.slice(0, 200),
        coverImage: coverImage.value || undefined,
        categoryId: selectedCategoryId.value,
        tagIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined,
        isOriginal: isOriginal.value,
      }
      const article = await createArticle(articleData)
      localStorage.removeItem('editor_draft')
      publishSuccess.value = true
      setTimeout(() => router.push(`/article/${article.id}`), 800)
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '发布失败，请稍后重试')
    }
  })
}

// 保存草稿
async function handleSaveDraft() {
  await withSaveLock(async () => {
    try {
      const data = {
        title: title.value || undefined,
        content: content.value || undefined,
        coverImage: coverImage.value || undefined,
        categoryId: selectedCategoryId.value,
        tagIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined,
      }
      if (articleId && isEditMode.value) {
        await updateDraftArticle(articleId, data)
      } else {
        await createDraftArticle(data)
      }
      localStorage.removeItem('editor_draft')
      isDirty.value = false
      autoSaveStatus.value = 'saved'
      toast.success('草稿已保存')
      setTimeout(() => { autoSaveStatus.value = '' }, 2000)
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '保存失败，请稍后重试')
    }
  })
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- 顶部工具栏 -->
    <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-4 mb-4">
      <div class="flex items-center gap-4 flex-wrap">
        <!-- 标题 -->
        <input
          v-model="title"
          type="text"
          placeholder="输入文章标题..."
          class="flex-1 min-w-60 text-lg font-bold text-text-primary outline-none
                 placeholder:text-text-placeholder/60"
          maxlength="100"
        />

        <div class="flex items-center gap-3 ml-auto">
          <!-- 原创开关 -->
          <label class="flex items-center gap-1.5 cursor-pointer">
            <input type="checkbox" :checked="isOriginal === 1" class="w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" @change="isOriginal = ($event.target as HTMLInputElement).checked ? 1 : 0" />
            <span class="text-xs text-text-secondary">原创</span>
          </label>

          <!-- 分类选择 -->
          <select
            v-model="selectedCategoryId"
            class="h-9 px-3 rounded-lg border border-gray-200 text-xs text-text-secondary
                   outline-none focus:border-brand/30 transition-colors bg-white"
          >
            <option :value="undefined">选择分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>

          <!-- 图片上传按钮 -->
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            multiple
            class="hidden"
            @change="handleFileInputChange"
          />
          <button
            :disabled="uploading"
            class="h-9 px-3 rounded-lg border border-gray-200 text-xs text-text-secondary
                   hover:border-brand/30 transition-colors bg-white flex items-center gap-1.5
                   disabled:opacity-50 disabled:cursor-not-allowed"
            title="插入图片（支持 Ctrl+V 粘贴或拖拽）"
            @click="triggerImagePicker"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <path d="M21 15l-5-5L5 21"/>
            </svg>
            <span>{{ uploading ? '上传中...' : '图片' }}</span>
          </button>

          <!-- 标签选择 -->
          <div class="relative group">
            <button class="h-9 px-3 rounded-lg border border-gray-200 text-xs text-text-secondary
                           hover:border-brand/30 transition-colors bg-white">
              标签 {{ selectedTagIds.length ? `(${selectedTagIds.length})` : '' }}
            </button>
            <div class="absolute top-full right-0 mt-2 w-60 bg-white rounded-xl shadow-lg border border-gray-100 p-3 z-10
                        opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200
                        max-h-48 overflow-y-auto">
              <button
                v-for="tag in tags"
                :key="tag.id"
                class="block w-full text-left text-xs py-1.5 px-2 rounded-lg transition-colors"
                :class="selectedTagIds.includes(tag.id)
                  ? 'bg-brand/5 text-brand font-medium'
                  : 'text-text-secondary hover:bg-gray-50'"
                @click="toggleTag(tag.id)"
              >
                {{ tag.name }}
              </button>
            </div>
          </div>

          <!-- 保存/发布 -->
          <button
            class="h-9 px-4 rounded-lg text-xs font-medium transition-all duration-200
                   border border-gray-200 text-text-secondary hover:border-brand/30"
            :disabled="saving"
            @click="handleSaveDraft"
          >
            {{ saving ? '保存中...' : '保存草稿' }}
          </button>
          <button
            :disabled="!title.trim() || !content.trim() || publishing"
            class="h-9 px-5 bg-brand text-white text-xs font-medium rounded-lg
                   hover:bg-brand-dark transition-all duration-200
                   disabled:opacity-40 disabled:cursor-not-allowed"
            @click="handlePublish"
          >
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>

      <!-- 封面图 -->
      <div class="mt-3">
        <input
          v-model="coverImage"
          type="url"
          placeholder="封面图 URL（可选）"
          class="w-full text-xs text-text-secondary outline-none bg-gray-50 rounded-lg px-3 py-2
                 placeholder:text-text-placeholder/60 focus:bg-gray-100 transition-colors"
        />
      </div>

      <!-- 自动保存状态 -->
      <div class="mt-2 text-xs flex items-center justify-between">
        <span class="text-text-placeholder">{{ charCount }} 字</span>
        <span
          v-if="autoSaveStatus === 'saving'"
          class="flex items-center gap-1 text-text-placeholder"
        >
          <svg class="animate-spin w-3 h-3" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          保存中...
        </span>
        <span v-else-if="autoSaveStatus === 'saved'" class="text-green flex items-center gap-1">
          <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6L9 17l-5-5"/></svg>
          已保存
        </span>
      </div>
    </div>

    <!-- 编辑/预览区 -->
    <div class="flex gap-4" style="height: calc(100vh - 240px)">
      <!-- 左侧编辑区 -->
      <div
        class="flex-1 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden"
        @drop.prevent="handleDrop"
        @dragover.prevent
      >
        <div class="flex items-center h-10 px-4 bg-gray-50 border-b border-gray-100">
          <span class="text-xs font-medium text-text-secondary tracking-wide">MARKDOWN</span>
        </div>
        <textarea
          ref="textareaRef"
          v-model="content"
          placeholder="开始写作... 支持 Markdown 语法（支持 Ctrl+V 粘贴图片）"
          class="w-full p-5 text-sm text-text-primary leading-relaxed outline-none resize-none
                 placeholder:text-text-placeholder/60"
          :style="{ height: 'calc(100% - 40px)' }"
          spellcheck="false"
          @paste="handlePaste"
        />
      </div>

      <!-- 右侧预览区 -->
      <div class="flex-1 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden hidden lg:block">
        <div class="flex items-center h-10 px-4 bg-gray-50 border-b border-gray-100">
          <span class="text-xs font-medium text-text-secondary tracking-wide">预览</span>
        </div>
        <div
          class="article-content max-w-none p-5 overflow-y-auto"
          :style="{ height: 'calc(100% - 40px)' }"
          v-html="previewHtml"
        />
      </div>
    </div>

    <!-- 发布成功提示 -->
    <Transition name="fade">
      <div v-if="publishSuccess" class="fixed inset-0 z-50 flex items-center justify-center bg-black/20">
        <div class="bg-white rounded-2xl p-10 text-center shadow-2xl">
          <svg class="w-16 h-16 text-green mx-auto mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><path d="M8 12l2 2 4-4"/>
          </svg>
          <p class="text-lg font-semibold text-text-primary mb-1">发布成功！</p>
          <p class="text-sm text-text-secondary mb-1">文章已提交审核，审核通过后将公开可见</p>
          <p class="text-xs text-text-placeholder">正在跳转到文章页...</p>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

/* 编辑器预览样式 */
:deep(.prose) {
  color: var(--color-text-primary);
}
:deep(.prose pre) {
  border-radius: 0.75rem;
  background: #1a1a2e;
  padding: 1rem;
  overflow-x: auto;
}
:deep(.prose code) {
  font-size: 0.82em;
}
:deep(.prose img) {
  border-radius: 0.75rem;
  max-width: 100%;
}
:deep(.prose h1),
:deep(.prose h2),
:deep(.prose h3) {
  color: var(--color-text-primary);
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}
:deep(.prose h1) { font-size: 1.6em; font-weight: 700; }
:deep(.prose h2) { font-size: 1.3em; font-weight: 600; border-bottom: 1px solid #f0f0f0; padding-bottom: 0.3em; }
:deep(.prose h3) { font-size: 1.1em; font-weight: 600; }
:deep(.prose blockquote) {
  border-left: 4px solid var(--color-brand);
  background: #f8fafb;
  padding: 0.5em 1em;
  border-radius: 0 0.5rem 0.5rem 0;
}
:deep(.prose a) { color: var(--color-brand); }
:deep(.prose table) { border-collapse: collapse; width: 100%; }
:deep(.prose th),
:deep(.prose td) { border: 1px solid #e5e7eb; padding: 0.5em 0.75em; text-align: left; }
:deep(.prose th) { background: #f9fafb; font-weight: 600; }
</style>
