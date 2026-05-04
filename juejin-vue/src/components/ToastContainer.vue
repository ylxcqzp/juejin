<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, dismiss } = useToast()
</script>

<template>
  <Teleport to="body">
    <div class="fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="pointer-events-auto max-w-sm rounded-xl px-5 py-3.5 shadow-lg backdrop-blur-sm
                 flex items-start gap-3 text-sm font-medium animate-in slide-in-from-right"
          :class="{
            'bg-red-500/95 text-white': toast.type === 'error',
            'bg-green/95 text-white': toast.type === 'success',
            'bg-gray-800/95 text-white': toast.type === 'info',
          }"
        >
          <!-- 图标 -->
          <svg v-if="toast.type === 'error'" class="w-5 h-5 flex-shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M15 9l-6 6M9 9l6 6"/>
          </svg>
          <svg v-else-if="toast.type === 'success'" class="w-5 h-5 flex-shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M8 12l2 2 4-4"/>
          </svg>
          <svg v-else class="w-5 h-5 flex-shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/>
          </svg>

          <span class="flex-1 leading-snug">{{ toast.message }}</span>

          <button class="flex-shrink-0 opacity-60 hover:opacity-100 transition-opacity cursor-pointer mt-0.5" @click="dismiss(toast.id)">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active { transition: all 0.3s ease-out; }
.toast-leave-active { transition: all 0.2s ease-in; }
.toast-enter-from { opacity: 0; transform: translateX(100px); }
.toast-leave-to { opacity: 0; transform: translateX(100px); }
</style>
