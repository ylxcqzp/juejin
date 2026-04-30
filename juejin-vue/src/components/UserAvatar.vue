<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  src?: string
  name?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
}>(), {
  size: 'md',
})

const sizeClass = computed(() => ({
  sm: 'w-7 h-7 text-xs',
  md: 'w-9 h-9 text-sm',
  lg: 'w-12 h-12 text-base',
  xl: 'w-20 h-20 text-2xl',
}[props.size]))

const initials = computed(() => {
  if (!props.name) return '?'
  return props.name.slice(0, 2).toUpperCase()
})
</script>

<template>
  <div
    :class="sizeClass"
    class="rounded-full bg-brand/10 text-brand font-semibold flex items-center justify-center overflow-hidden shrink-0"
  >
    <img
      v-if="src"
      :src="src"
      :alt="name ?? ''"
      class="w-full h-full object-cover"
    />
    <span v-else>{{ initials }}</span>
  </div>
</template>
