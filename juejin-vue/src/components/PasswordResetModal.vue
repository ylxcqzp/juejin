<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  visible: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', data: { phone: string; code: string; password: string }): void
}>()

const phone = ref('')
const verificationCode = ref('')
const newPassword = ref('')
const countdown = ref(0)
const isSending = ref(false)

const canSendCode = computed(() => countdown.value === 0 && !isSending.value && phone.value.length === 11)
const canSubmit = computed(() => phone.value.length === 11 && verificationCode.value.length === 6 && newPassword.value.length >= 6)

/**
 * 发送验证码
 */
async function handleSendCode() {
  if (!canSendCode.value) return
  
  isSending.value = true
  // 模拟发送验证码
  await new Promise(resolve => setTimeout(resolve, 1000))
  isSending.value = false
  
  // 开始倒计时
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

/**
 * 提交
 */
function handleSubmit() {
  if (!canSubmit.value) return
  emit('confirm', {
    phone: phone.value,
    code: verificationCode.value,
    password: newPassword.value
  })
  closeModal()
}

/**
 * 关闭弹框
 */
function closeModal() {
  phone.value = ''
  verificationCode.value = ''
  newPassword.value = ''
  countdown.value = 0
  emit('update:visible', false)
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- 遮罩 -->
        <div class="absolute inset-0 bg-black/40" @click="closeModal" />
        
        <!-- 弹框 -->
        <div class="relative bg-white rounded-lg shadow-xl w-96 p-6">
          <!-- 关闭按钮 -->
          <button
            class="absolute top-4 right-4 text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
            @click="closeModal"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>

          <!-- 标题 -->
          <h3 class="text-lg font-bold text-text-primary mb-6">手机重置密码</h3>

          <!-- 手机号输入 -->
          <div class="flex items-center gap-2 mb-4">
            <div class="relative">
              <select class="h-10 pl-3 pr-8 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors appearance-none bg-white cursor-pointer">
                <option value="+86">+86</option>
                <option value="+852">+852</option>
                <option value="+853">+853</option>
                <option value="+886">+886</option>
              </select>
              <svg class="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
              </svg>
            </div>
            <input
              v-model="phone"
              type="tel"
              maxlength="11"
              placeholder="请输入手机号码"
              class="flex-1 h-10 px-4 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors"
            />
          </div>

          <!-- 验证码输入 -->
          <div class="flex items-center gap-3 mb-4">
            <input
              v-model="verificationCode"
              type="text"
              maxlength="6"
              placeholder="验证码"
              class="flex-1 h-10 px-4 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors"
            />
            <button
              class="px-4 py-2 text-sm text-brand whitespace-nowrap cursor-pointer disabled:text-text-secondary disabled:cursor-not-allowed"
              :disabled="!canSendCode"
              @click="handleSendCode"
            >
              {{ isSending ? '发送中...' : countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </button>
          </div>

          <!-- 新密码输入 -->
          <div class="mb-6">
            <input
              v-model="newPassword"
              type="password"
              placeholder="请输入新密码"
              class="w-full h-10 px-4 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors"
            />
          </div>

          <!-- 修改按钮 -->
          <button
            class="w-full h-10 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-dark transition-all disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer mb-4"
            :disabled="!canSubmit"
            @click="handleSubmit"
          >
            修改
          </button>

          <!-- 邮箱重置密码链接 -->
          <div class="text-center">
            <button class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer">
              邮箱重置密码
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
