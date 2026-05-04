import request from './request'
import type { TaskVO } from '@/types'

export function getAllTasks() {
  return request.get('/tasks').then(r => r.data.data as TaskVO[])
}

export function getTasksByType(type: 'daily' | 'newbie') {
  return request.get(`/tasks/type/${type}`).then(r => r.data.data as TaskVO[])
}

export function claimTaskReward(taskId: number) {
  return request.post(`/tasks/${taskId}/claim`).then(r => r.data.data as TaskVO)
}
