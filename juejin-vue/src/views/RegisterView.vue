<script setup lang="ts">
import { reactive, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const step = ref<'email' | 'phone'>('email')

const form = reactive({
  email: '',
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false,
})

const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 邮箱注册校验
const emailValid = computed(() => {
  if (step.value !== 'email') return false
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
    && form.nickname.length >= 2
    && form.password.length >= 6
    && form.password === form.confirmPassword
    && form.agreeTerms
})

// 手机注册校验
const phoneValid = computed(() => {
  if (step.value !== 'phone') return false
  return /^1[3-9]\d{9}$/.test(form.phone)
    && form.nickname.length >= 2
    && form.password.length >= 6
    && form.password === form.confirmPassword
    && form.agreeTerms
})

const isValid = computed(() => (step.value === 'email' ? emailValid.value : phoneValid.value))

// 密码不匹配提示
const passwordMismatch = computed(() =>
  form.confirmPassword.length > 0 && form.password !== form.confirmPassword,
)

async function handleRegister() {
  if (!isValid.value) return

  loading.value = true
  errorMsg.value = ''

  try {
    let res
    if (step.value === 'email') {
      res = await request.post('/users/register', {
        email: form.email,
        password: form.password,
        nickname: form.nickname,
      })
    } else {
      res = await request.post('/users/register/phone', {
        phone: form.phone,
        password: form.password,
        nickname: form.nickname,
      })
    }

    if (res.data.code !== 200) {
      errorMsg.value = res.data.message || '注册失败'
      return
    }

    successMsg.value = '注册成功！正在为你跳转...'

    const account = step.value === 'email' ? form.email : form.phone
    await authStore.login(account, form.password, step.value)
    router.push('/')
  } catch (e: unknown) {
    errorMsg.value = e instanceof Error ? e.message : '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-text-primary text-center mb-2">加入掘金</h1>
    <p class="text-sm text-text-secondary text-center mb-6">注册账号，开启你的技术成长之旅</p>

    <!-- 注册方式切换 -->
    <div class="flex bg-gray-100 rounded-lg p-1 mb-6">
      <button
        v-for="opt in [{ k: 'email' as const, label: '邮箱注册' }, { k: 'phone' as const, label: '手机注册' }]"
        :key="opt.k"
        class="flex-1 py-2 text-sm rounded-md transition-all"
        :class="step === opt.k ? 'bg-white text-text-primary font-medium shadow-sm' : 'text-text-secondary'"
        @click="step = opt.k; errorMsg = ''"
      >
        {{ opt.label }}
      </button>
    </div>

    <!-- 表单 -->
    <form class="space-y-4" @submit.prevent="handleRegister">
      <!-- 邮箱 -->
      <div v-if="step === 'email'">
        <input
          v-model="form.email"
          type="email"
          placeholder="请输入邮箱"
          autocomplete="email"
          class="w-full h-11 px-4 rounded-lg border border-gray-200 text-sm outline-none transition-all
                 focus:border-brand focus:ring-1 focus:ring-brand placeholder:text-text-placeholder"
        />
      </div>

      <!-- 手机号 -->
      <div v-if="step === 'phone'">
        <input
          v-model="form.phone"
          type="tel"
          placeholder="请输入手机号"
          autocomplete="tel"
          class="w-full h-11 px-4 rounded-lg border border-gray-200 text-sm outline-none transition-all
                 focus:border-brand focus:ring-1 focus:ring-brand placeholder:text-text-placeholder"
        />
      </div>

      <!-- 昵称 -->
      <div>
        <input
          v-model="form.nickname"
          type="text"
          placeholder="给自己取一个昵称（至少2个字符）"
          autocomplete="nickname"
          class="w-full h-11 px-4 rounded-lg border border-gray-200 text-sm outline-none transition-all
                 focus:border-brand focus:ring-1 focus:ring-brand placeholder:text-text-placeholder"
        />
      </div>

      <!-- 密码 -->
      <div>
        <input
          v-model="form.password"
          type="password"
          placeholder="设置密码（至少6位）"
          autocomplete="new-password"
          class="w-full h-11 px-4 rounded-lg border border-gray-200 text-sm outline-none transition-all
                 focus:border-brand focus:ring-1 focus:ring-brand placeholder:text-text-placeholder"
        />
      </div>

      <!-- 确认密码 -->
      <div>
        <input
          v-model="form.confirmPassword"
          type="password"
          placeholder="再次输入密码"
          autocomplete="new-password"
          class="w-full h-11 px-4 rounded-lg border text-sm outline-none transition-all
                 placeholder:text-text-placeholder"
          :class="passwordMismatch ? 'border-red-400 focus:border-red-400 focus:ring-red-400' : 'border-gray-200 focus:border-brand focus:ring-1 focus:ring-brand'"
        />
        <Transition name="fade">
          <p v-if="passwordMismatch" class="text-xs text-red-500 mt-1.5 ml-1">两次输入的密码不一致</p>
        </Transition>
      </div>

      <!-- 协议 -->
      <label class="flex items-start gap-2 cursor-pointer">
        <input v-model="form.agreeTerms" type="checkbox" class="mt-0.5 w-4 h-4 rounded border-gray-300 text-brand focus:ring-brand" />
        <span class="text-xs text-text-secondary">
          阅读并同意 <a href="#" class="text-brand hover:underline">服务协议</a> 和 <a href="#" class="text-brand hover:underline">隐私政策</a>
        </span>
      </label>

      <!-- 提示 -->
      <Transition name="fade">
        <p v-if="errorMsg" class="text-sm text-red-500 bg-red-50 rounded-lg px-3 py-2">{{ errorMsg }}</p>
        <p v-else-if="successMsg" class="text-sm text-green bg-green-50 rounded-lg px-3 py-2">{{ successMsg }}</p>
      </Transition>

      <!-- 注册按钮 -->
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
        {{ loading ? '注册中...' : '注册' }}
      </button>
    </form>

    <p class="text-center text-sm text-text-secondary mt-6">
      已有账号？<router-link to="/login" class="text-brand hover:underline font-medium">去登录</router-link>
    </p>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
