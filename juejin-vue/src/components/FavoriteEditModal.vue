<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { FavoriteFolderVO } from '@/types'

/**
 * 收藏集编辑/新建弹窗组件
 * 支持新建和编辑两种模式，通过 folder prop 区分
 */
const props = defineProps<{
  visible: boolean
  folder: FavoriteFolderVO | null // 编辑模式传入收藏集对象，新建模式传 null
}>()

const emit = defineEmits<{
  confirm: [data: { name: string; description: string; isPublic: boolean }]
  cancel: []
  'update:visible': [value: boolean]
}>()

// 表单数据
const name = ref('')
const description = ref('')
const isPublic = ref(true)

// 是否为编辑模式
const isEdit = computed(() => props.folder !== null)

// 弹窗标题
const modalTitle = computed(() => isEdit.value ? '编辑收藏集' : '新建收藏集')

// 描述字数统计
const descLength = computed(() => description.value.length)
const MAX_DESC_LENGTH = 100

/**
 * 监听 visible 变化，打开时初始化表单数据
 */
watch(() => props.visible, (val) => {
  if (val) {
    if (props.folder) {
      // 编辑模式：回填数据
      name.value = props.folder.name
      description.value = props.folder.description || ''
      isPublic.value = props.folder.isPublic
    } else {
      // 新建模式：重置表单
      name.value = ''
      description.value = ''
      isPublic.value = true
    }
  }
})

/**
 * 清空名称输入框
 */
function clearName() {
  name.value = ''
}

/**
 * 关闭弹窗
 */
function handleClose() {
  emit('update:visible', false)
  emit('cancel')
}

/**
 * 确认提交
 */
function handleConfirm() {
  const trimmedName = name.value.trim()
  if (!trimmedName) return
  emit('confirm', {
    name: trimmedName,
    description: description.value.trim(),
    isPublic: isPublic.value,
  })
  handleClose()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center" @click.self="handleClose">
        <!-- 遮罩层 -->
        <div class="absolute inset-0 bg-black/40" />

        <!-- 弹窗主体 -->
        <div class="relative bg-white rounded-xl shadow-2xl w-[440px] max-w-[90vw] overflow-hidden">
          <!-- 标题栏 -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h3 class="text-base font-semibold text-text-primary">{{ modalTitle }}</h3>
            <button class="text-text-placeholder hover:text-text-secondary transition-colors" @click="handleClose">
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- 表单内容 -->
          <div class="px-6 py-5 space-y-5">
            <!-- 名称输入 -->
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1.5">名称</label>
              <div class="relative">
                <input
                  v-model="name"
                  type="text"
                  placeholder="请输入收藏集名称"
                  maxlength="20"
                  class="w-full h-10 pl-3 pr-9 rounded-lg border border-gray-200 text-sm text-text-primary
                         placeholder:text-text-placeholder focus:border-brand focus:ring-2 focus:ring-brand/20
                         outline-none transition-all"
                />
                <!-- 清空按钮 -->
                <button
                  v-if="name"
                  class="absolute right-2.5 top-1/2 -translate-y-1/2 text-text-placeholder hover:text-text-secondary transition-colors"
                  @click="clearName"
                >
                  <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="9"/><path d="M8 8l8 8M16 8l-8 8"/>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 描述输入 -->
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1.5">描述</label>
              <textarea
                v-model="description"
                placeholder="请输入收藏描述（限100字，选填）"
                :maxlength="MAX_DESC_LENGTH"
                rows="3"
                class="w-full px-3 py-2.5 rounded-lg border border-gray-200 text-sm text-text-primary resize-none
                       placeholder:text-text-placeholder focus:border-brand focus:ring-2 focus:ring-brand/20
                       outline-none transition-all leading-relaxed"
              />
              <div class="flex justify-end mt-1">
                <span class="text-xs text-text-placeholder">{{ descLength }}/{{ MAX_DESC_LENGTH }}</span>
              </div>
            </div>

            <!-- 公开/隐私选择 -->
            <div>
              <label class="block text-sm font-medium text-text-primary mb-2">可见性</label>
              <div class="flex items-center gap-6">
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input
                    v-model="isPublic"
                    type="radio"
                    :value="true"
                    class="w-4 h-4 text-brand border-gray-300 focus:ring-brand/20"
                  />
                  <span class="text-sm text-text-primary group-hover:text-brand transition-colors">公开</span>
                </label>
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input
                    v-model="isPublic"
                    type="radio"
                    :value="false"
                    class="w-4 h-4 text-brand border-gray-300 focus:ring-brand/20"
                  />
                  <span class="text-sm text-text-primary group-hover:text-brand transition-colors">隐私</span>
                </label>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-100 bg-gray-50/50">
            <button
              class="h-9 px-5 rounded-lg text-sm font-medium text-text-secondary hover:bg-gray-100 transition-colors"
              @click="handleClose"
            >
              取消
            </button>
            <button
              class="h-9 px-5 rounded-lg text-sm font-medium text-white bg-brand hover:bg-brand-dark disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              :disabled="!name.trim()"
              @click="handleConfirm"
            >
              确定
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active { transition: opacity 0.25s ease; }
.modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from,
.modal-leave-to { opacity: 0; }

.modal-enter-active .relative,
.modal-leave-active .relative {
  transition: transform 0.25s ease, opacity 0.25s ease;
}
.modal-enter-from .relative,
.modal-leave-to .relative {
  transform: scale(0.95);
  opacity: 0;
}
</style>
