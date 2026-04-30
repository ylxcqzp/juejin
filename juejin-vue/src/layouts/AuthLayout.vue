<script setup lang="ts">
// 认证页面布局：纯居中展示，无导航栏
</script>

<template>
  <div class="min-h-screen bg-bg flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <a href="/" class="inline-flex items-center gap-2 text-2xl font-bold text-brand">
          <span class="w-9 h-9 bg-brand rounded-xl flex items-center justify-center text-white text-lg font-extrabold">J</span>
          掘金
        </a>
        <p class="mt-2 text-text-secondary text-sm">一个面向开发者的技术社区</p>
      </div>

      <!-- 内容区 -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <slot />
      </div>

      <!-- 底部链接 -->
      <p class="text-center text-xs text-text-placeholder mt-6">
        登录即表示同意
        <a href="#" class="text-brand hover:underline">服务协议</a>
        和
        <a href="#" class="text-brand hover:underline">隐私政策</a>
      </p>
    </div>
  </div>
</template>
