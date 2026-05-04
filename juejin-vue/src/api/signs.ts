import request from './request'
import type { SignVO } from '@/types'

export function signIn() {
  return request.post('/signs').then(r => r.data.data as SignVO)
}

export function getSignStatus() {
  return request.get('/signs/status').then(r => r.data.data as SignVO)
}
