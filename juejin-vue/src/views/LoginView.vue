<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSubmitLock } from '@/composables/useSubmitLock'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginType = ref<'email' | 'phone'>('email')

const form = reactive({
  account: '',
  password: '',
})

const { isSubmitting: loading, withLock: withLoginLock } = useSubmitLock()
const errorMsg = ref('')
const redirectPath = computed(() => (route.query.redirect as string) || '/')

const isValid = computed(() => {
  if (loginType.value === 'email') {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.account) && form.password.length >= 6
  }
  return /^1[3-9]\d{9}$/.test(form.account) && form.password.length >= 6
})

async function handleLogin() {
  if (!isValid.value) return
  errorMsg.value = ''

  await withLoginLock(async () => {
    try {
      await authStore.login(form.account, form.password, loginType.value)
      router.push(redirectPath.value)
    } catch (e: unknown) {
      errorMsg.value = e instanceof Error ? e.message : '登录失败，请稍后重试'
    }
  })
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-text-primary text-center mb-2">欢迎回来</h1>
    <p class="text-sm text-text-secondary text-center mb-6">登录你的掘金账号，探索技术世界</p>

    <!-- 登录方式切换 -->
    <div class="flex bg-gray-100 rounded-lg p-1 mb-6">
      <button
        v-for="opt in [{ k: 'email' as const, label: '邮箱登录' }, { k: 'phone' as const, label: '手机登录' }]"
        :key="opt.k"
        class="flex-1 py-2 text-sm rounded-md transition-all"
        :class="loginType === opt.k ? 'bg-white text-text-primary font-medium shadow-sm' : 'text-text-secondary'"
        @click="loginType = opt.k"
      >
        {{ opt.label }}
      </button>
    </div>

    <!-- 表单 -->
    <form class="space-y-4" @submit.prevent="handleLogin">
      <div>
        <input
          v-model="form.account"
          :type="loginType === 'email' ? 'email' : 'tel'"
          :placeholder="loginType === 'email' ? '请输入邮箱' : '请输入手机号'"
          autocomplete="username"
          class="w-full h-11 px-4 rounded-lg border border-gray-200 text-sm outline-none transition-all
                 focus:border-brand focus:ring-1 focus:ring-brand placeholder:text-text-placeholder"
        />
      </div>

      <div>
        <input
          v-model="form.password"
          type="password"
          placeholder="请输入密码（至少6位）"
          autocomplete="current-password"
          class="w-full h-11 px-4 rounded-lg border border-gray-200 text-sm outline-none transition-all
                 focus:border-brand focus:ring-1 focus:ring-brand placeholder:text-text-placeholder"
        />
      </div>

      <Transition name="fade">
        <p v-if="errorMsg" class="text-sm text-red-500 bg-red-50 rounded-lg px-3 py-2">{{ errorMsg }}</p>
      </Transition>

      <button
        type="submit"
        :disabled="!isValid || loading"
        class="w-full h-11 bg-brand text-white text-sm font-medium rounded-lg transition-all
               hover:bg-brand-dark disabled:opacity-50 disabled:cursor-not-allowed
               flex items-center justify-center gap-2"
      >
        <svg v-if="loading" class="animate-spin w-4 h-4" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>

    <p class="text-center text-sm text-text-secondary mt-6">
      还没有账号？<router-link to="/register" class="text-brand hover:underline font-medium">立即注册</router-link>
    </p>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
