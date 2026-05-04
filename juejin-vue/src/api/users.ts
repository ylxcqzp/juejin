import request from './request'
import type { UserVO, UserProfileVO, PointsVO, FollowStatusVO, FollowUserVO, PageResult, BadgeVO, UserBadgeVO } from '@/types'

export function getUserById(id: number) {
  return request.get(`/users/${id}`).then(r => r.data.data as UserVO)
}

export function getCurrentUser() {
  return request.get('/users/current').then(r => r.data.data as UserVO)
}

export function getUserProfile(id: number) {
  return request.get(`/users/${id}/profile`).then(r => r.data.data as UserProfileVO)
}

export function getMyProfile() {
  return request.get('/users/profile').then(r => r.data.data as UserProfileVO)
}

export function updateProfile(data: { nickname?: string; avatar?: string; bio?: string; backgroundImage?: string }) {
  return request.put('/users/profile', data)
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/users/password', data)
}

export function updateSkillTags(tagIds: number[]) {
  return request.put('/users/tags', tagIds)
}

export function updateSocialLinks(links: { linkType: string; linkUrl: string }[]) {
  return request.put('/users/social-links', { links })
}

export function getMyPoints() {
  return request.get('/users/points').then(r => r.data.data as PointsVO)
}

export function getMyBadges() {
  return request.get('/users/badges').then(r => r.data.data as UserBadgeVO[])
}

export function getAllBadges() {
  return request.get('/users/badges/all').then(r => r.data.data as BadgeVO[])
}

export function bindPhone(account: string, code: string) {
  return request.post('/users/bind/phone', { account, code })
}

export function bindEmail(account: string, code: string) {
  return request.post('/users/bind/email', { account, code })
}

export function unbindAccount(type: 'phone' | 'email') {
  return request.delete(`/users/unbind/${type}`)
}

export function getPrivacySettings() {
  return request.get('/users/privacy').then(r => r.data.data)
}

export function updatePrivacySettings(data: {
  showFavorites?: boolean
  showFollowing?: boolean
  showFollowers?: boolean
  allowStrangerMessage?: boolean
  messagePushEnabled?: boolean
}) {
  return request.put('/users/privacy', data)
}

// 关注
export function followUser(userId: number) {
  return request.post(`/users/${userId}/follow`)
}

export function unfollowUser(userId: number) {
  return request.delete(`/users/${userId}/follow`)
}

export function getFollowingList(userId: number, page: number, size: number) {
  return request.get(`/users/${userId}/following`, { params: { page, size } })
    .then(r => r.data.data as PageResult<FollowUserVO>)
}

export function getFollowerList(userId: number, page: number, size: number) {
  return request.get(`/users/${userId}/followers`, { params: { page, size } })
    .then(r => r.data.data as PageResult<FollowUserVO>)
}

export function removeFollower(userId: number) {
  return request.delete(`/users/followers/${userId}`)
}

export function getFollowStatus(userId: number) {
  return request.get(`/users/${userId}/follow/status`).then(r => r.data.data as FollowStatusVO)
}
