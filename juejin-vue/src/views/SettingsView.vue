<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { updateProfile, changePassword, updatePrivacySettings, getMyProfile, updateSocialLinks, followUser, unfollowUser, getFollowingList } from '@/api/users'
import { getTagList, followTag, unfollowTag, getFollowingTags } from '@/api/tags'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import { useAuthStore } from '@/stores/auth'
import type { UserProfileVO, TagVO, FollowUserVO } from '@/types'
import PhoneChangeModal from '@/components/PhoneChangeModal.vue'
import PasswordResetModal from '@/components/PasswordResetModal.vue'

const toast = useToast()
const authStore = useAuthStore()
const router = useRouter()

type SectionKey = 'profile' | 'account' | 'message' | 'block' | 'tag'
const activeSection = ref<SectionKey>('profile')



// 屏蔽管理Tab
type BlockTab = 'users' | 'tags'
const activeBlockTab = ref<BlockTab>('users')

// 标签管理Tab
type TagManageTab = 'following' | 'all'
const activeTagManageTab = ref<TagManageTab>('following')

// 标签搜索关键词
const tagSearchKeyword = ref('')

const { isSubmitting: saving, withLock: withSaveLock } = useSubmitLock()
const saved = ref(false)

// 弹框显示状态
const showPhoneChangeModal = ref(false)
const showPasswordResetModal = ref(false)

// ============ 个人资料 ============
const profileForm = reactive({
  nickname: '',
  bio: '',
  avatar: '',
  backgroundImage: '',
  title: '',
  // 仅用于UI展示，后端可能没有这些字段
  jobTitle: '',
  company: '',
  personalWebsite: '',
  startWorkDate: '',
  careerDirection: '',
})

// ============ 账号设置 ============
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordError = ref('')
const emailForm = reactive({ email: '' })
const phoneForm = reactive({ phone: '' })

// ============ 消息设置 ============
const messageSettings = reactive({
  // 私信权限: all-所有人 following-我关注的人 mutual-互相关注的人 none-关闭
  privateMessagePermission: 'all' as 'all' | 'following' | 'mutual' | 'none',
})

// ============ 屏蔽管理 ============
const blockedUsers = ref<FollowUserVO[]>([])
const blockedTags = ref<TagVO[]>([])
const blockUsersLoading = ref(false)
const blockTagsLoading = ref(false)

// ============ 标签管理 ============
const followingTags = ref<TagVO[]>([])
const allTags = ref<TagVO[]>([])
const tagManageLoading = ref(false)

const sections = [
  { key: 'profile' as SectionKey, label: '个人资料', icon: 'user' },
  { key: 'account' as SectionKey, label: '账号设置', icon: 'shield' },
  { key: 'message' as SectionKey, label: '消息设置', icon: 'bell' },
  { key: 'block' as SectionKey, label: '屏蔽管理', icon: 'ban' },
  { key: 'tag' as SectionKey, label: '标签管理', icon: 'tag' },
]

const sectionIcons: Record<string, string> = {
  user: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>`,
  shield: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>`,
  bell: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>`,
  ban: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>`,
  tag: `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>`,
}

/**
 * 加载所有设置数据
 */
async function loadSettings() {
  try {
    const profile = await getMyProfile()
    profileForm.nickname = profile.nickname || ''
    profileForm.bio = profile.bio || ''
    profileForm.avatar = profile.avatar || ''
    profileForm.backgroundImage = profile.backgroundImage || ''
    profileForm.title = profile.title || ''
    // 其他字段后端可能没有，保持为空
    profileForm.jobTitle = (profile as any).jobTitle || ''
    profileForm.company = (profile as any).company || ''
    profileForm.personalWebsite = (profile as any).personalWebsite || ''
    profileForm.startWorkDate = (profile as any).startWorkDate || ''
    profileForm.careerDirection = (profile as any).careerDirection || ''
  } catch {
    toast.error('个人资料加载失败，请刷新页面重试')
  }
}

/**
 * 保存个人资料
 */
async function handleSaveProfile() {
  saved.value = false
  await withSaveLock(async () => {
    try {
      await updateProfile({
        nickname: profileForm.nickname,
        bio: profileForm.bio,
        avatar: profileForm.avatar,
        backgroundImage: profileForm.backgroundImage,
      })
      saved.value = true
      setTimeout(() => { saved.value = false }, 2000)
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '保存失败')
    }
  })
}

/**
 * 修改密码
 */
async function handleChangePassword() {
  passwordError.value = ''
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = '两次输入的密码不一致'
    return
  }
  if (passwordForm.newPassword.length < 6) {
    passwordError.value = '密码至少6位'
    return
  }
  await withSaveLock(async () => {
    try {
      await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      saved.value = true
      setTimeout(() => { saved.value = false }, 2000)
    } catch (e: unknown) {
      passwordError.value = e instanceof Error ? e.message : '修改失败'
    }
  })
}

/**
 * 保存消息设置
 */
async function handleSaveMessageSettings() {
  saved.value = false
  await withSaveLock(async () => {
    try {
      await updatePrivacySettings({
        messagePushEnabled: messageSettings.receiveSystemNotify,
      })
      saved.value = true
      setTimeout(() => { saved.value = false }, 2000)
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '保存失败')
    }
  })
}

/**
 * 加载屏蔽的用户列表
 */
async function loadBlockedUsers() {
  blockUsersLoading.value = true
  try {
    const data = await getFollowingList(authStore.userId || 1, 1, 50)
    blockedUsers.value = data.list
  } catch {
    blockedUsers.value = []
  } finally {
    blockUsersLoading.value = false
  }
}

/**
 * 加载屏蔽的标签列表
 */
async function loadBlockedTags() {
  blockTagsLoading.value = true
  try {
    const data = await getFollowingTags(authStore.userId || 1)
    blockedTags.value = data
  } catch {
    blockedTags.value = []
  } finally {
    blockTagsLoading.value = false
  }
}

/**
 * 取消屏蔽用户
 */
async function handleUnblockUser(userId: number) {
  try {
    await unfollowUser(userId)
    blockedUsers.value = blockedUsers.value.filter(u => u.userId !== userId)
    toast.success('已取消屏蔽')
  } catch {
    toast.error('操作失败')
  }
}

/**
 * 取消屏蔽标签
 */
async function handleUnblockTag(tagId: number) {
  try {
    await unfollowTag(tagId)
    blockedTags.value = blockedTags.value.filter(t => t.id !== tagId)
    toast.success('已取消屏蔽')
  } catch {
    toast.error('操作失败')
  }
}

/**
 * 加载已关注标签
 */
async function loadFollowingTags() {
  tagManageLoading.value = true
  try {
    const data = await getFollowingTags(authStore.userId || 1)
    followingTags.value = data
  } catch {
    followingTags.value = []
  } finally {
    tagManageLoading.value = false
  }
}

/**
 * 加载全部标签
 */
async function loadAllTags() {
  tagManageLoading.value = true
  try {
    const data = await getTagList({ page: 1, size: 100 })
    allTags.value = data.list
  } catch {
    allTags.value = []
  } finally {
    tagManageLoading.value = false
  }
}

/**
 * 关注/取消关注标签
 */
async function handleToggleFollowTag(tag: TagVO & { isFollowing?: boolean }) {
  const wasFollowing = tag.isFollowing
  tag.isFollowing = !wasFollowing
  try {
    if (wasFollowing) {
      await unfollowTag(tag.id)
    } else {
      await followTag(tag.id)
    }
  } catch {
    tag.isFollowing = wasFollowing
    toast.error('操作失败')
  }
}

function getSectionIcon(icon: string) {
  return sectionIcons[icon] || sectionIcons.user
}

// 监听屏蔽管理Tab切换
async function handleBlockTabChange(tab: BlockTab) {
  activeBlockTab.value = tab
  if (tab === 'users') {
    await loadBlockedUsers()
  } else {
    await loadBlockedTags()
  }
}

// 监听标签管理Tab切换
async function handleTagManageTabChange(tab: TagManageTab) {
  activeTagManageTab.value = tab
  if (tab === 'following') {
    await loadFollowingTags()
  } else {
    await loadAllTags()
  }
}

onMounted(async () => {
  await loadSettings()
  // 默认加载已关注的标签
  await loadFollowingTags()
})

// 标签管理：设置isFollowing属性
const followingTagsWithStatus = computed(() => {
  return followingTags.value.map(tag => ({ ...tag, isFollowing: true }))
})

// 全部标签（带搜索过滤）
const allTagsWithStatus = computed(() => {
  const followedIds = new Set(followingTags.value.map(t => t.id))
  let tags = allTags.value.map(tag => ({ ...tag, isFollowing: followedIds.has(tag.id) }))
  // 搜索过滤
  if (tagSearchKeyword.value.trim()) {
    const keyword = tagSearchKeyword.value.trim().toLowerCase()
    tags = tags.filter(tag =>
      tag.name.toLowerCase().includes(keyword) ||
      (tag.description && tag.description.toLowerCase().includes(keyword))
    )
  }
  return tags
})
</script>

<template>
  <div class="min-h-screen bg-bg">
    <!-- 页面标题 -->
    <div class="bg-white border-b border-gray-100">
      <div class="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
        <h1 class="text-lg font-bold text-text-primary">设置</h1>
        <!-- 返回个人主页 -->
        <button
          class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer flex items-center gap-1"
          @click="router.push(`/user/${authStore.userId}`)"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
          </svg>
          返回个人主页
        </button>
      </div>
    </div>

    <div class="max-w-5xl mx-auto px-6 py-6">
      <div class="flex gap-8">
        <!-- 左侧导航 -->
        <nav class="w-52 flex-shrink-0">
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-2 space-y-1">
            <button
              v-for="s in sections"
              :key="s.key"
              class="w-full text-left px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer flex items-center gap-3"
              :class="activeSection === s.key
                ? 'bg-brand/5 text-brand'
                : 'text-text-secondary hover:bg-gray-50 hover:text-text-primary'"
              @click="activeSection = s.key"
            >
              <span v-html="getSectionIcon(s.icon)" class="flex-shrink-0" />
              {{ s.label }}
            </button>
          </div>
        </nav>

        <!-- 内容区 -->
        <div class="flex-1 min-w-0">
          <!-- 个人资料 -->
          <div v-if="activeSection === 'profile'" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
            <!-- 标题 -->
            <div class="px-6 py-4 border-b border-gray-100">
              <h2 class="text-base font-semibold text-text-primary">个人资料</h2>
            </div>

            <div class="p-6">
              <!-- 基本信息标题 -->
              <h3 class="text-sm font-medium text-text-primary mb-6">基本信息</h3>

              <div class="flex gap-8">
                <!-- 左侧表单 -->
                <div class="flex-1 space-y-5">
                  <!-- 用户名 -->
                  <div class="flex items-center gap-4">
                    <label class="w-20 text-sm text-text-primary text-right">
                      <span class="text-red-500 mr-1">*</span>用户名
                    </label>
                    <div class="flex-1 relative">
                      <input
                        v-model="profileForm.nickname"
                        type="text"
                        maxlength="20"
                        class="w-full h-10 px-4 pr-16 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none focus:border-brand/30 transition-colors"
                      />
                      <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-text-secondary">
                        {{ profileForm.nickname.length }}/20
                      </span>
                    </div>
                  </div>

                  <!-- 开始工作 -->
                  <div class="flex items-center gap-4">
                    <label class="w-20 text-sm text-text-primary text-right">
                      <span class="text-red-500 mr-1">*</span>开始工作
                    </label>
                    <div class="flex-1 relative">
                      <input
                        v-model="profileForm.startWorkDate"
                        type="text"
                        placeholder="请选择日期"
                        class="w-full h-10 px-4 pl-10 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none focus:border-brand/30 transition-colors"
                      />
                      <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke-width="2"/>
                        <line x1="16" y1="2" x2="16" y2="6" stroke-width="2"/>
                        <line x1="8" y1="2" x2="8" y2="6" stroke-width="2"/>
                        <line x1="3" y1="10" x2="21" y2="10" stroke-width="2"/>
                      </svg>
                    </div>
                  </div>

                  <!-- 职业方向 -->
                  <div class="flex items-center gap-4">
                    <label class="w-20 text-sm text-text-primary text-right">
                      <span class="text-red-500 mr-1">*</span>职业方向
                    </label>
                    <div class="flex-1 relative">
                      <select
                        v-model="profileForm.careerDirection"
                        class="w-full h-10 px-4 pr-10 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none focus:border-brand/30 transition-colors appearance-none cursor-pointer"
                      >
                        <option value="">请选择</option>
                        <option value="frontend">前端</option>
                        <option value="backend">后端</option>
                        <option value="mobile">移动端</option>
                        <option value="ai">人工智能</option>
                        <option value="data">数据</option>
                        <option value="devops">运维</option>
                        <option value="test">测试</option>
                        <option value="product">产品</option>
                        <option value="design">设计</option>
                      </select>
                      <svg class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                      </svg>
                    </div>
                  </div>

                  <!-- 职位 -->
                  <div class="flex items-center gap-4">
                    <label class="w-20 text-sm text-text-primary text-right">职位</label>
                    <div class="flex-1 relative">
                      <input
                        v-model="profileForm.jobTitle"
                        type="text"
                        maxlength="50"
                        placeholder="请输入你的职位"
                        class="w-full h-10 px-4 pr-16 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none focus:border-brand/30 transition-colors"
                      />
                      <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-text-secondary">
                        {{ profileForm.jobTitle.length }}/50
                      </span>
                    </div>
                  </div>

                  <!-- 公司 -->
                  <div class="flex items-center gap-4">
                    <label class="w-20 text-sm text-text-primary text-right">公司</label>
                    <div class="flex-1 relative">
                      <input
                        v-model="profileForm.company"
                        type="text"
                        maxlength="50"
                        placeholder="请输入你的公司"
                        class="w-full h-10 px-4 pr-16 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none focus:border-brand/30 transition-colors"
                      />
                      <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-text-secondary">
                        {{ profileForm.company.length }}/50
                      </span>
                    </div>
                  </div>

                  <!-- 个人主页 -->
                  <div class="flex items-center gap-4">
                    <label class="w-20 text-sm text-text-primary text-right">个人主页</label>
                    <div class="flex-1 relative">
                      <input
                        v-model="profileForm.personalWebsite"
                        type="text"
                        maxlength="100"
                        placeholder="请输入你的个人主页"
                        class="w-full h-10 px-4 pr-16 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none focus:border-brand/30 transition-colors"
                      />
                      <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-text-secondary">
                        {{ profileForm.personalWebsite.length }}/100
                      </span>
                    </div>
                  </div>

                  <!-- 个人介绍 -->
                  <div class="flex items-start gap-4">
                    <label class="w-20 text-sm text-text-primary text-right pt-2">个人介绍</label>
                    <div class="flex-1 relative">
                      <textarea
                        v-model="profileForm.bio"
                        rows="4"
                        maxlength="100"
                        placeholder="请填写职业技能、擅长的事情、兴趣爱好等"
                        class="w-full p-3 pr-16 rounded-lg bg-gray-50 border border-gray-100 text-sm outline-none resize-none focus:border-brand/30 transition-colors"
                      />
                      <span class="absolute right-3 bottom-3 text-xs text-text-secondary">
                        {{ profileForm.bio.length }}/100
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 右侧头像 -->
                <div class="w-40 flex-shrink-0 flex flex-col items-center">
                  <div class="w-20 h-20 rounded-full bg-gray-100 overflow-hidden mb-3">
                    <img v-if="profileForm.avatar" :src="profileForm.avatar" alt="头像" class="w-full h-full object-cover" />
                    <span v-else class="w-full h-full flex items-center justify-center text-2xl font-bold text-gray-400">{{ profileForm.nickname?.charAt(0) || 'U' }}</span>
                  </div>
                  <p class="text-sm text-text-primary mb-1">上传头像</p>
                  <p class="text-xs text-text-secondary text-center leading-relaxed">
                    格式：支持JPG、PNG、JPEG<br/>大小：5M以内
                  </p>
                </div>
              </div>

              <!-- 保存按钮 -->
              <div class="flex items-center gap-3 pt-6 mt-6 border-t border-gray-100">
                <button
                  class="px-6 py-2 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-dark transition-all disabled:opacity-40 cursor-pointer"
                  :disabled="saving"
                  @click="handleSaveProfile"
                >
                  {{ saving ? '保存中...' : '保存' }}
                </button>
                <Transition name="fade">
                  <span v-if="saved" class="text-sm text-green-500">✓ 已保存</span>
                </Transition>
              </div>
            </div>
          </div>

          <!-- 账号设置 -->
          <div v-else-if="activeSection === 'account'" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
            <!-- 标题 -->
            <div class="px-6 py-4 border-b border-gray-100">
              <h2 class="text-lg font-bold text-text-primary">账号设置</h2>
            </div>

            <div class="divide-y divide-gray-100">
              <!-- 手机 -->
              <div class="flex items-center justify-between px-6 py-5">
                <div class="flex items-center gap-16">
                  <span class="text-sm text-text-primary w-16">手机</span>
                  <span class="text-sm text-text-secondary">15****7749</span>
                </div>
                <button
                  class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer"
                  @click="showPhoneChangeModal = true"
                >
                  换绑
                </button>
              </div>

              <!-- 微信 -->
              <div class="flex items-center justify-between px-6 py-5">
                <div class="flex items-center gap-16">
                  <span class="text-sm text-text-primary w-16">微信</span>
                  <span class="text-sm text-text-secondary">未绑定</span>
                </div>
                <button class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer">
                  绑定
                </button>
              </div>

              <!-- 新浪微博 -->
              <div class="flex items-center justify-between px-6 py-5">
                <div class="flex items-center gap-16">
                  <span class="text-sm text-text-primary w-16">新浪微博</span>
                  <span class="text-sm text-text-secondary">未绑定</span>
                </div>
                <button class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer">
                  绑定
                </button>
              </div>

              <!-- GitHub -->
              <div class="flex items-center justify-between px-6 py-5">
                <div class="flex items-center gap-16">
                  <span class="text-sm text-text-primary w-16">GitHub</span>
                  <span class="text-sm text-text-secondary">未绑定</span>
                </div>
                <button class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer">
                  绑定
                </button>
              </div>

              <!-- 密码 -->
              <div class="flex items-center justify-between px-6 py-5">
                <div class="flex items-center gap-16">
                  <span class="text-sm text-text-primary w-16">密码</span>
                  <span class="text-sm text-text-secondary"></span>
                </div>
                <button
                  class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer"
                  @click="showPasswordResetModal = true"
                >
                  重置
                </button>
              </div>

              <!-- 账号注销 -->
              <div class="flex items-center justify-between px-6 py-5">
                <div class="flex items-center gap-16">
                  <span class="text-sm text-text-primary w-16">账号注销</span>
                  <span class="text-sm text-text-secondary"></span>
                </div>
                <button class="text-sm text-brand hover:text-brand-dark transition-colors cursor-pointer">
                  注销
                </button>
              </div>
            </div>
          </div>

          <!-- 消息设置 -->
          <div v-else-if="activeSection === 'message'" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
            <!-- 标题 -->
            <div class="px-6 py-4 border-b border-gray-100">
              <h2 class="text-lg font-semibold text-text-primary">消息设置</h2>
            </div>

            <div class="p-6">
              <!-- 私信设置 -->
              <h3 class="text-sm font-medium text-text-primary mb-4">私信设置</h3>

              <!-- 允许谁给我发私信 -->
              <div class="flex items-center gap-8">
                <span class="text-sm text-text-primary">允许谁给我发私信</span>
                <div class="flex items-center gap-6">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="messageSettings.privateMessagePermission"
                      type="radio"
                      value="all"
                      class="w-4 h-4 text-brand border-gray-300 focus:ring-brand"
                    />
                    <span class="text-sm text-text-primary">所有人</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="messageSettings.privateMessagePermission"
                      type="radio"
                      value="following"
                      class="w-4 h-4 text-brand border-gray-300 focus:ring-brand"
                    />
                    <span class="text-sm text-text-primary">我关注的人</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="messageSettings.privateMessagePermission"
                      type="radio"
                      value="mutual"
                      class="w-4 h-4 text-brand border-gray-300 focus:ring-brand"
                    />
                    <span class="text-sm text-text-primary">互相关注的人</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="messageSettings.privateMessagePermission"
                      type="radio"
                      value="none"
                      class="w-4 h-4 text-brand border-gray-300 focus:ring-brand"
                    />
                    <span class="text-sm text-text-primary">关闭</span>
                    <span class="text-sm text-text-secondary">（不允许任何人给我发私信）</span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- 屏蔽管理 -->
          <div v-else-if="activeSection === 'block'" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
            <!-- Tab切换 -->
            <div class="flex items-center gap-6 px-6 pt-4 border-b border-gray-100">
              <button
                v-for="tab in [
                  { key: 'users' as BlockTab, label: '屏蔽作者' },
                  { key: 'tags' as BlockTab, label: '屏蔽标签' },
                ]"
                :key="tab.key"
                class="pb-3 text-sm font-medium transition-colors relative"
                :class="activeBlockTab === tab.key ? 'text-brand' : 'text-text-secondary hover:text-text-primary'"
                @click="handleBlockTabChange(tab.key)"
              >
                {{ tab.label }}
                <span v-if="activeBlockTab === tab.key" class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand rounded-full" />
              </button>
            </div>

            <!-- 屏蔽的作者列表 -->
            <div v-if="activeBlockTab === 'users'" class="p-6">
              <div v-if="blockUsersLoading" class="flex justify-center py-12">
                <svg class="animate-spin w-6 h-6 text-brand" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
              </div>
              <div v-else-if="blockedUsers.length > 0" class="grid grid-cols-5 gap-4">
                <div
                  v-for="user in blockedUsers"
                  :key="user.userId"
                  class="flex flex-col items-center p-4 border border-gray-100 rounded-lg hover:shadow-md transition-shadow"
                >
                  <!-- 头像 -->
                  <div class="w-14 h-14 rounded-full bg-gray-100 overflow-hidden mb-3">
                    <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname" class="w-full h-full object-cover" />
                    <span v-else class="w-full h-full flex items-center justify-center text-lg font-bold text-gray-400">{{ user.nickname?.charAt(0) || 'U' }}</span>
                  </div>
                  <!-- 昵称和等级 -->
                  <div class="flex items-center gap-1 mb-2">
                    <span class="text-sm text-text-primary truncate max-w-[80px]">{{ user.nickname }}</span>
                    <span class="px-1 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-500">Lv.{{ (user as any).level || 5 }}</span>
                  </div>
                  <!-- 屏蔽时间 -->
                  <p class="text-xs text-text-secondary mb-3">屏蔽时间: {{ new Date().toISOString().slice(0, 10) }}</p>
                  <!-- 取消屏蔽按钮 -->
                  <button
                    class="px-6 py-1.5 text-sm text-brand border border-brand rounded hover:bg-brand/5 transition-colors cursor-pointer"
                    @click="handleUnblockUser(user.userId)"
                  >
                    取消屏蔽
                  </button>
                </div>
              </div>
              <div v-else class="text-center py-12">
                <p class="text-sm text-text-placeholder">暂无屏蔽的作者</p>
              </div>
            </div>

            <!-- 屏蔽的标签列表 -->
            <div v-else class="p-6">
              <div v-if="blockTagsLoading" class="flex justify-center py-12">
                <svg class="animate-spin w-6 h-6 text-brand" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
              </div>
              <div v-else-if="blockedTags.length > 0" class="grid grid-cols-5 gap-4">
                <div
                  v-for="tag in blockedTags"
                  :key="tag.id"
                  class="flex flex-col items-center p-4 border border-gray-100 rounded-lg hover:shadow-md transition-shadow"
                >
                  <!-- 标签图标 -->
                  <div class="w-14 h-14 rounded-full bg-gray-100 flex items-center justify-center mb-3">
                    <img v-if="tag.icon" :src="tag.icon" :alt="tag.name" class="w-full h-full object-cover rounded-full" />
                    <span v-else class="text-lg font-bold text-brand">{{ tag.name?.charAt(0) || 'T' }}</span>
                  </div>
                  <!-- 标签名 -->
                  <span class="text-sm text-text-primary mb-2">{{ tag.name }}</span>
                  <!-- 屏蔽时间 -->
                  <p class="text-xs text-text-secondary mb-3">屏蔽时间: {{ new Date().toISOString().slice(0, 10) }}</p>
                  <!-- 取消屏蔽按钮 -->
                  <button
                    class="px-6 py-1.5 text-sm text-brand border border-brand rounded hover:bg-brand/5 transition-colors cursor-pointer"
                    @click="handleUnblockTag(tag.id)"
                  >
                    取消屏蔽
                  </button>
                </div>
              </div>
              <div v-else class="text-center py-12">
                <p class="text-sm text-text-placeholder">暂无屏蔽的标签</p>
              </div>
            </div>
          </div>

          <!-- 标签管理 -->
          <div v-else-if="activeSection === 'tag'" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-6">
            <h2 class="text-base font-semibold text-text-primary mb-6">标签管理</h2>

            <!-- Tab切换 -->
            <div class="flex items-center gap-6 mb-6 border-b border-gray-100">
              <button
                v-for="tab in [
                  { key: 'following' as TagManageTab, label: '已关注标签' },
                  { key: 'all' as TagManageTab, label: '全部标签' },
                ]"
                :key="tab.key"
                class="pb-3 text-sm font-medium transition-colors relative"
                :class="activeTagManageTab === tab.key ? 'text-brand' : 'text-text-secondary hover:text-text-primary'"
                @click="handleTagManageTabChange(tab.key)"
              >
                {{ tab.label }}
                <span v-if="activeTagManageTab === tab.key" class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand rounded-full" />
              </button>
            </div>

            <!-- 已关注标签 -->
            <div v-if="activeTagManageTab === 'following'">
              <div v-if="tagManageLoading" class="flex justify-center py-12">
                <svg class="animate-spin w-6 h-6 text-brand" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
              </div>
              <div v-else-if="followingTagsWithStatus.length > 0" class="space-y-3">
                <div
                  v-for="tag in followingTagsWithStatus"
                  :key="tag.id"
                  class="flex items-center justify-between py-3"
                >
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center">
                      <span class="text-lg font-bold text-brand">{{ tag.name?.charAt(0) || 'T' }}</span>
                    </div>
                    <div>
                      <p class="text-sm text-text-primary">{{ tag.name }}</p>
                      <p v-if="tag.description" class="text-xs text-text-secondary">{{ tag.description }}</p>
                    </div>
                  </div>
                  <button
                    class="px-4 py-1.5 text-xs font-medium transition-colors cursor-pointer min-w-[80px]"
                    :class="tag.isFollowing
                      ? 'bg-[#8BC34A] text-white hover:bg-[#7CB342]'
                      : 'bg-gray-100 text-text-secondary hover:bg-gray-200'"
                    @click="handleToggleFollowTag(tag)"
                  >
                    {{ tag.isFollowing ? '已关注' : '+ 关注' }}
                  </button>
                </div>
              </div>
              <div v-else class="text-center py-12">
                <p class="text-sm text-text-placeholder">暂无关注的标签</p>
              </div>
            </div>

            <!-- 全部标签 -->
            <div v-else>
              <!-- 搜索框 -->
              <div class="mb-4">
                <div class="relative max-w-md">
                  <input
                    v-model="tagSearchKeyword"
                    type="text"
                    placeholder="搜索标签"
                    class="w-full h-10 pl-10 pr-4 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors"
                  />
                  <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <circle cx="11" cy="11" r="8" stroke-width="2"/>
                    <path d="M21 21l-4.35-4.35" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
              </div>

              <div v-if="tagManageLoading" class="flex justify-center py-12">
                <svg class="animate-spin w-6 h-6 text-brand" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
              </div>
              <div v-else-if="allTagsWithStatus.length > 0" class="space-y-3">
                <div
                  v-for="tag in allTagsWithStatus"
                  :key="tag.id"
                  class="flex items-center justify-between py-3"
                >
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center">
                      <span class="text-lg font-bold text-brand">{{ tag.name?.charAt(0) || 'T' }}</span>
                    </div>
                    <div>
                      <p class="text-sm text-text-primary">{{ tag.name }}</p>
                      <p v-if="tag.description" class="text-xs text-text-secondary">{{ tag.description }}</p>
                    </div>
                  </div>
                  <button
                    class="px-4 py-1.5 text-xs font-medium transition-colors cursor-pointer min-w-[80px]"
                    :class="tag.isFollowing
                      ? 'bg-[#8BC34A] text-white hover:bg-[#7CB342]'
                      : 'bg-gray-100 text-text-secondary hover:bg-gray-200'"
                    @click="handleToggleFollowTag(tag)"
                  >
                    {{ tag.isFollowing ? '已关注' : '+ 关注' }}
                  </button>
                </div>
              </div>
              <div v-else class="text-center py-12">
                <p class="text-sm text-text-placeholder">暂无标签</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 手机换绑弹框 -->
    <PhoneChangeModal
      v-model:visible="showPhoneChangeModal"
      phone="15****7749"
    />

    <!-- 密码重置弹框 -->
    <PasswordResetModal
      v-model:visible="showPasswordResetModal"
    />
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
