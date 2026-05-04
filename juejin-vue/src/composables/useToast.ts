import { ref } from 'vue'

export interface Toast {
  id: number
  message: string
  type: 'error' | 'success' | 'info'
}

const toasts = ref<Toast[]>([])
let nextId = 0

export function useToast() {
  function show(message: string, type: Toast['type'] = 'error') {
    const id = nextId++
    toasts.value.push({ id, message, type })
    // 自动消失（success 2秒，error 5秒）
    const duration = type === 'success' ? 2000 : 5000
    setTimeout(() => dismiss(id), duration)
    return id
  }

  function error(message: string) {
    return show(message, 'error')
  }

  function success(message: string) {
    return show(message, 'success')
  }

  function info(message: string) {
    return show(message, 'info')
  }

  function dismiss(id: number) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  return { toasts, show, error, success, info, dismiss }
}
