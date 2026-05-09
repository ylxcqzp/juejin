import { ref } from 'vue'

/**
 * 表单提交锁 — 防止重复提交，同时提供 loading 状态
 *
 * Usage:
 *   const { isSubmitting, withLock } = useSubmitLock()
 *   async function handleSubmit() {
 *     await withLock(async () => {
 *       await someApiCall()
 *     })
 *   }
 *   // 模板中: :disabled="isSubmitting" 或 :loading="isSubmitting"
 */
export function useSubmitLock() {
  const isSubmitting = ref(false)

  async function withLock<T>(fn: () => Promise<T>): Promise<T | undefined> {
    if (isSubmitting.value) return
    isSubmitting.value = true
    try {
      return await fn()
    } finally {
      isSubmitting.value = false
    }
  }

  return { isSubmitting, withLock }
}
