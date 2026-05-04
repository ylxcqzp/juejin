import request from './request'

export function toggleLike(targetId: number, targetType: number) {
  return request.post('/likes', { targetId, targetType })
}

export function getLikeStatus(targetId: number, targetType: number) {
  return request.get('/likes/status', { params: { targetId, targetType } })
    .then(r => r.data.data as boolean)
}
