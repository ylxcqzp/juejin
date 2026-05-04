<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { updateProfile, changePassword, updatePrivacySettings, getMyProfile, updateSocialLinks } from '@/api/users'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const activeSection = ref<'profile' | 'account' | 'privacy' | 'social'>('profile')
const saving = ref(false)
const saved = ref(false)

// 资料表单
const profileForm = reactive({ nickname: '', bio: '', avatar: '', backgroundImage: '' })

// 密码表单
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordError = ref('')

// 隐私表单
const privacyForm = reactive({
  showFavorites: true,
  showFollowing: true,
  showFollowers: true,
  allowStrangerMessage: false,
  messagePushEnabled: true,
})

// 社交链接
const socialLinks = ref<{ linkType: string; linkUrl: string }[]>([])

const sections = [
  { key: 'profile' as const, label: '个人资料', icon: '👤' },
  { key: 'account' as const, label: '账号安全', icon: '🔒' },
  { key: 'privacy' as const, label: '隐私设置', icon: '🛡️' },
  { key: 'social' as const, label: '社交链接', icon: '🔗' },
]

async function loadSettings() {
  try {
    const profile = await getMyProfile()
    profileForm.nickname = profile.nickname || ''
    profileForm.bio = profile.bio || ''
    profileForm.avatar = profile.avatar || ''
    profileForm.backgroundImage = profile.backgroundImage || ''
    if (profile.privacy) Object.assign(privacyForm, profile.privacy)
    if (profile.socialLinks) socialLinks.value = profile.socialLinks
  } catch {
    toast.error('个人资料加载失败，请刷新页面重试')
  }
}

async function handleSaveProfile() {
  ''
  saving.value = true
  saved.value = false
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
  } finally { saving.value = false }
}

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
  saving.value = true
  try {
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    saved.value = true
    setTimeout(() => { saved.value = false }, 2000)
  } catch (e: any) {
    passwordError.value = e.message || '修改失败'
  }
  finally { saving.value = false }
}

async function handleSavePrivacy() {
  ''
  saving.value = true
  try {
    await updatePrivacySettings(privacyForm)
    saved.value = true
    setTimeout(() => { saved.value = false }, 2000)
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '保存失败')
  } finally { saving.value = false }
}

async function handleSaveSocialLinks() {
  ''
  saving.value = true
  try {
    await updateSocialLinks(socialLinks.value)
    saved.value = true
    setTimeout(() => { saved.value = false }, 2000)
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '保存失败')
  } finally { saving.value = false }
}

function addSocialLink() {
  socialLinks.value.push({ linkType: '', linkUrl: '' })
}

function removeSocialLink(index: number) {
  socialLinks.value.splice(index, 1)
}

onMounted(loadSettings)
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <h1 class="text-lg font-bold text-text-primary mb-6">设置</h1>

    <div class="flex gap-6">
      <!-- 侧边导航 -->
      <nav class="w-44 flex-shrink-0 space-y-1">
        <button
          v-for="s in sections"
          :key="s.key"
          class="w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 cursor-pointer flex items-center gap-2"
          :class="activeSection === s.key
            ? 'bg-brand/5 text-brand'
            : 'text-text-secondary hover:bg-gray-50'"
          @click="activeSection = s.key"
        >
          <span>{{ s.icon }}</span> {{ s.label }}
        </button>
      </nav>

      <!-- 内容区 -->
      <div class="flex-1 min-w-0 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-6">
        <!-- 个人资料 -->
        <template v-if="activeSection === 'profile'">
          <h2 class="text-sm font-semibold text-text-primary mb-4">个人资料</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-xs text-text-secondary mb-1.5">昵称</label>
              <input v-model="profileForm.nickname" type="text" minlength="2" maxlength="20" class="w-full h-10 px-3 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors" />
            </div>
            <div>
              <label class="block text-xs text-text-secondary mb-1.5">个人简介</label>
              <textarea v-model="profileForm.bio" rows="3" maxlength="200" class="w-full p-3 rounded-lg border border-gray-200 text-sm outline-none resize-none focus:border-brand/30 transition-colors" />
            </div>
            <div>
              <label class="block text-xs text-text-secondary mb-1.5">头像 URL</label>
              <input v-model="profileForm.avatar" type="url" class="w-full h-10 px-3 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors" />
            </div>
            <div class="flex items-center gap-3">
              <button
                class="px-6 py-2 bg-brand text-white text-xs font-medium rounded-lg hover:bg-brand-dark transition-all disabled:opacity-40 cursor-pointer"
                :disabled="saving"
                @click="handleSaveProfile"
              >
                {{ saving ? '保存中...' : '保存' }}
              </button>
              <Transition name="fade">
                <span v-if="saved" class="text-xs text-green">✓ 已保存</span>
              </Transition>
            </div>
          </div>
        </template>

        <!-- 账号安全 -->
        <template v-else-if="activeSection === 'account'">
          <h2 class="text-sm font-semibold text-text-primary mb-4">修改密码</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-xs text-text-secondary mb-1.5">原密码</label>
              <input v-model="passwordForm.oldPassword" type="password" class="w-full h-10 px-3 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors" />
            </div>
            <div>
              <label class="block text-xs text-text-secondary mb-1.5">新密码</label>
              <input v-model="passwordForm.newPassword" type="password" class="w-full h-10 px-3 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors" />
            </div>
            <div>
              <label class="block text-xs text-text-secondary mb-1.5">确认新密码</label>
              <input v-model="passwordForm.confirmPassword" type="password" class="w-full h-10 px-3 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30 transition-colors" />
            </div>
            <Transition name="fade">
              <p v-if="passwordError" class="text-xs text-red-500">{{ passwordError }}</p>
            </Transition>
            <div class="flex items-center gap-3">
              <button
                class="px-6 py-2 bg-brand text-white text-xs font-medium rounded-lg hover:bg-brand-dark transition-all disabled:opacity-40 cursor-pointer"
                :disabled="saving"
                @click="handleChangePassword"
              >
                修改密码
              </button>
              <Transition name="fade">
                <span v-if="saved" class="text-xs text-green">✓ 已保存</span>
              </Transition>
            </div>
          </div>
        </template>

        <!-- 隐私设置 -->
        <template v-else-if="activeSection === 'privacy'">
          <h2 class="text-sm font-semibold text-text-primary mb-4">隐私设置</h2>
          <div class="space-y-4">
            <label v-for="opt in [
              { k: 'showFavorites' as const, l: '公开收藏夹' },
              { k: 'showFollowing' as const, l: '公开关注列表' },
              { k: 'showFollowers' as const, l: '公开粉丝列表' },
              { k: 'allowStrangerMessage' as const, l: '允许陌生人私信' },
              { k: 'messagePushEnabled' as const, l: '消息推送' },
            ]" :key="opt.k" class="flex items-center justify-between py-2 cursor-pointer">
              <span class="text-sm text-text-primary">{{ opt.l }}</span>
              <div
                class="w-10 h-6 rounded-full transition-colors duration-200 cursor-pointer relative"
                :class="privacyForm[opt.k] ? 'bg-brand' : 'bg-gray-200'"
                @click="privacyForm[opt.k] = !privacyForm[opt.k]"
              >
                <div
                  class="absolute top-0.5 w-5 h-5 bg-white rounded-full shadow-sm transition-all duration-200"
                  :class="privacyForm[opt.k] ? 'left-[18px]' : 'left-0.5'"
                />
              </div>
            </label>
            <button
              class="px-6 py-2 bg-brand text-white text-xs font-medium rounded-lg hover:bg-brand-dark transition-all disabled:opacity-40 cursor-pointer"
              :disabled="saving"
              @click="handleSavePrivacy"
            >
              保存
            </button>
          </div>
        </template>

        <!-- 社交链接 -->
        <template v-else-if="activeSection === 'social'">
          <h2 class="text-sm font-semibold text-text-primary mb-4">社交链接</h2>
          <div class="space-y-3">
            <div v-for="(link, i) in socialLinks" :key="i" class="flex gap-2">
              <select v-model="link.linkType" class="h-10 px-3 rounded-lg border border-gray-200 text-xs outline-none focus:border-brand/30">
                <option value="">选择平台</option>
                <option value="github">GitHub</option>
                <option value="blog">Blog</option>
                <option value="weibo">Weibo</option>
                <option value="twitter">Twitter</option>
              </select>
              <input v-model="link.linkUrl" type="url" placeholder="链接地址" class="flex-1 h-10 px-3 rounded-lg border border-gray-200 text-sm outline-none focus:border-brand/30" />
              <button class="px-3 text-text-placeholder hover:text-red-500 transition-colors cursor-pointer" @click="removeSocialLink(i)">✕</button>
            </div>
            <button class="text-xs text-brand hover:underline cursor-pointer" @click="addSocialLink">+ 添加链接</button>
            <div class="pt-2">
              <button
                class="px-6 py-2 bg-brand text-white text-xs font-medium rounded-lg hover:bg-brand-dark transition-all disabled:opacity-40 cursor-pointer"
                :disabled="saving"
                @click="handleSaveSocialLinks"
              >
                保存
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
