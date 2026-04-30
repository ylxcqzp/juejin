<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<template>
  <div class="min-h-screen bg-bg flex flex-col">
    <AppHeader />
    <main class="flex-1">
      <slot />
    </main>
    <!-- Footer 后续补充 -->
  </div>
</template>
