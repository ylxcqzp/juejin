<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserProfile, getFollowStatus, followUser, unfollowUser, getFollowingList, getFollowerList } from '@/api/users'
import { getUserArticles } from '@/api/articles'
import { getPinList } from '@/api/pins'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { UserProfileVO, ArticleVO, PinVO, FollowUserVO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const userId = computed(() => Number(route.params.id))

// 资料
const profile = ref<UserProfileVO | null>(null)
const followStatus = ref({ isFollowing: false, isFollowedBy: false })
const loading = ref(true)
const error = ref('')

// Tab 切换
type TabKey = 'articles' | 'pins' | 'favorites'
const activeTab = ref<TabKey>('articles')

// 列表数据
const articles = ref<ArticleVO[]>([])
const pins = ref<PinVO[]>([])
const articlePage = ref(1)
const articleTotal = ref(0)
const pinPage = ref(1)
const listLoading = ref(false)
const listFinished = ref(false)

const { isSubmitting: following, withLock: withFollowLock } = useSubmitLock()

// 关注/粉丝弹窗
const followModal = ref<'following' | 'followers' | null>(null)
const followList = ref<FollowUserVO[]>([])
const followListLoading = ref(false)

async function loadProfile() {
  loading.value = true
  error.value = ''
  try {
    const [profileData, fStatus] = await Promise.all([
      getUserProfile(userId.value),
      authStore.isLoggedIn ? getFollowStatus(userId.value) : Promise.resolve(null),
    ])
    profile.value = profileData
    if (fStatus) followStatus.value = fStatus
  } catch {
    error.value = '用户不存在或加载失败'
  } finally {
    loading.value = false
  }
}

async function loadList() {
  if (listLoading.value || listFinished.value) return
  listLoading.value = true
  try {
    if (activeTab.value === 'articles') {
      const data = await getUserArticles(userId.value, { page: articlePage.value, size: 10 })
      articles.value.push(...data.list)
      articleTotal.value = data.total
      if (data.list.length === 0 || articles.value.length >= data.total) listFinished.value = true
      articlePage.value++
    } else if (activeTab.value === 'pins') {
      const data = await getPinList({ page: pinPage.value, size: 10 })
      pins.value.push(...data.list)
      if (data.list.length === 0) listFinished.value = true
      pinPage.value++
    }
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '内容加载失败')
  } finally { listLoading.value = false }
}

function resetAndLoad() {
  articles.value = []
  pins.value = []
  articlePage.value = 1
  pinPage.value = 1
  listFinished.value = false
  loadList()
}

watch(activeTab, resetAndLoad)

async function handleFollow() {
  if (!authStore.isLoggedIn) { router.push({ name: 'login', query: { redirect: route.fullPath } }); return }
  await withFollowLock(async () => {
    const wasFollowing = followStatus.value.isFollowing
    followStatus.value.isFollowing = !wasFollowing
    if (profile.value) profile.value.followerCount += wasFollowing ? -1 : 1
    try {
      if (wasFollowing) await unfollowUser(userId.value)
      else await followUser(userId.value)
    } catch (e: unknown) {
      followStatus.value.isFollowing = wasFollowing
      if (profile.value) profile.value.followerCount += wasFollowing ? 1 : -1
      toast.error(e instanceof Error ? e.message : '操作失败')
    }
  })
}

async function openFollowModal(type: 'following' | 'followers') {
  followModal.value = type
  followListLoading.value = true
  try {
    const fetcher = type === 'following' ? getFollowingList : getFollowerList
    const data = await fetcher(userId.value, 1, 50)
    followList.value = data.list
  } catch { followList.value = [] }
  finally { followListLoading.value = false }
}

function closeFollowModal() {
  followModal.value = null
  followList.value = []
}

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function timeAgo(d: string): string {
  const diff = Date.now() - new Date(d).getTime()
  const h = Math.floor(diff / 3600000)
  if (h < 24) return h <= 0 ? '刚刚' : `${h}小时前`
  const days = Math.floor(h / 24)
  if (days < 30) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN')
}

onMounted(async () => {
  await loadProfile()
  loadList()
})

const tabs: { key: TabKey; label: string }[] = [
  { key: 'articles', label: '文章' },
  { key: 'pins', label: '沸点' },
  { key: 'favorites', label: '收藏' },
]
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <!-- 加载/错误 -->
    <div v-if="loading" class="flex justify-center py-20">
      <svg class="animate-spin w-8 h-8 text-brand" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <div v-else-if="error" class="text-center py-20">
      <p class="text-text-secondary text-sm">{{ error }}</p>
    </div>

    <template v-else-if="profile">
      <!-- 用户信息卡 -->
      <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden mb-6">
        <!-- 背景图 -->
        <div
          class="h-36 bg-gradient-to-r from-brand/5 via-brand/15 to-brand-light/10"
          :style="profile.backgroundImage ? `background-image: url(${profile.backgroundImage}); background-size: cover; background-position: center;` : ''"
        />

        <div class="px-6 pb-6 -mt-10">
          <div class="flex items-end gap-5">
            <!-- 头像 -->
            <div class="w-20 h-20 rounded-full ring-4 ring-white bg-gradient-to-br from-brand/30 to-brand-light/40
                        flex items-center justify-center text-2xl font-bold text-brand flex-shrink-0 shadow-lg">
              {{ profile.nickname?.charAt(0) || 'U' }}
            </div>

            <div class="flex-1 pt-10">
              <div class="flex items-center gap-3">
                <h1 class="text-xl font-bold text-text-primary">{{ profile.nickname }}</h1>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-gradient-to-r from-brand/10 to-brand-light/10 text-brand">
                  Lv{{ profile.level }}
                </span>
              </div>
              <p v-if="profile.bio" class="text-sm text-text-secondary mt-1">{{ profile.bio }}</p>
            </div>

            <!-- 关注按钮 -->
            <button
              v-if="authStore.userId && authStore.userId !== userId"
              class="px-5 py-2 rounded-full text-sm font-medium transition-all duration-200"
              :class="followStatus.isFollowing
                ? 'bg-gray-100 text-text-secondary hover:bg-gray-200'
                : 'bg-brand text-white hover:bg-brand-dark'"
              :disabled="following"
              @click="handleFollow"
            >
              <svg v-if="following" class="animate-spin w-3.5 h-3.5 inline mr-1" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              {{ followStatus.isFollowing ? '已关注' : '+ 关注' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 数据统计 -->
      <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5 mb-6">
        <div class="grid grid-cols-4 gap-4 text-center">
          <button class="cursor-pointer group" @click="openFollowModal('following')">
            <p class="text-lg font-bold text-text-primary group-hover:text-brand transition-colors">{{ formatCount(profile.followingCount) }}</p>
            <p class="text-xs text-text-secondary mt-0.5">关注</p>
          </button>
          <button class="cursor-pointer group" @click="openFollowModal('followers')">
            <p class="text-lg font-bold text-text-primary group-hover:text-brand transition-colors">{{ formatCount(profile.followerCount) }}</p>
            <p class="text-xs text-text-secondary mt-0.5">粉丝</p>
          </button>
          <div>
            <p class="text-lg font-bold text-text-primary">{{ formatCount(profile.articleCount) }}</p>
            <p class="text-xs text-text-secondary mt-0.5">文章</p>
          </div>
          <div>
            <p class="text-lg font-bold text-text-primary">{{ formatCount(profile.likeCount) }}</p>
            <p class="text-xs text-text-secondary mt-0.5">获赞</p>
          </div>
        </div>

        <!-- 技能标签 -->
        <div v-if="profile.tags?.length" class="flex items-center gap-2 mt-4 pt-4 border-t border-gray-50 flex-wrap">
          <span class="text-xs text-text-placeholder">技能:</span>
          <span
            v-for="tag in profile.tags"
            :key="tag.id"
            class="px-2.5 py-1 rounded-full text-xs bg-brand/5 text-brand font-medium"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>

      <!-- Tab 切换 -->
      <div class="flex items-center gap-8 mb-6 border-b border-gray-100/80">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="relative pb-3 text-sm font-medium transition-all duration-200 cursor-pointer"
          :class="activeTab === tab.key ? 'text-text-primary' : 'text-text-secondary hover:text-text-primary'"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span
            v-if="activeTab === tab.key"
            class="absolute bottom-0 left-0 right-0 h-0.5 bg-brand rounded-full transition-all duration-300"
          />
        </button>
      </div>

      <!-- 文章列表 -->
      <div v-if="activeTab === 'articles'" class="space-y-3">
        <article
          v-for="article in articles"
          :key="article.id"
          class="bg-white rounded-xl p-4 flex gap-4 cursor-pointer
                 shadow-[0_1px_3px_rgba(0,0,0,0.04)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)]
                 hover:-translate-y-0.5 transition-all duration-300"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div v-if="article.coverImage" class="w-28 h-20 rounded-lg overflow-hidden flex-shrink-0">
            <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" loading="lazy" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1.5">
              <h3 class="text-sm font-semibold text-text-primary line-clamp-1 hover:text-brand transition-colors flex-1 min-w-0">{{ article.title }}</h3>
              <span
                v-if="authStore.userId === userId && article.status !== 2"
                class="flex-shrink-0 px-2 py-0.5 rounded-full text-[10px] font-medium"
                :class="{
                  'bg-yellow-50 text-yellow-600': article.status === 1,
                  'bg-red-50 text-red-500': article.status === 3,
                }"
              >
                {{ { 1: '审核中', 3: '已驳回' }[article.status] }}
              </span>
            </div>
            <p class="text-xs text-text-secondary line-clamp-1 mb-2">{{ article.summary }}</p>
            <div class="flex items-center gap-4 text-xs text-text-secondary/70">
              <span class="flex items-center gap-0.5">
                <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="2"/><path d="M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5z"/></svg>
                {{ formatCount(article.viewCount) }}
              </span>
              <span class="flex items-center gap-0.5">
                <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                {{ formatCount(article.likeCount) }}
              </span>
              <span>{{ timeAgo(article.publishTime || article.createTime) }}</span>
            </div>
          </div>
        </article>

        <div v-if="articles.length === 0 && !listLoading" class="text-center py-12">
          <p class="text-sm text-text-placeholder">暂无文章</p>
        </div>
      </div>

      <!-- 沸点列表 -->
      <div v-else-if="activeTab === 'pins'" class="space-y-3">
        <div
          v-for="pin in pins"
          :key="pin.id"
          class="bg-white rounded-xl p-4 shadow-[0_1px_3px_rgba(0,0,0,0.04)]"
        >
          <p class="text-sm text-text-primary leading-relaxed mb-3">{{ pin.content }}</p>
          <div class="flex items-center gap-4 text-xs text-text-secondary">
            <span class="flex items-center gap-0.5">
              <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
              {{ formatCount(pin.likeCount) }}
            </span>
            <span class="flex items-center gap-0.5">
              <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              {{ formatCount(pin.commentCount) }}
            </span>
            <span>{{ timeAgo(pin.createTime) }}</span>
          </div>
        </div>
        <div v-if="pins.length === 0 && !listLoading" class="text-center py-12">
          <p class="text-sm text-text-placeholder">暂无沸点</p>
        </div>
      </div>

      <!-- 收藏（仅自己可见） -->
      <div v-else-if="activeTab === 'favorites'" class="text-center py-12">
        <p class="text-sm text-text-placeholder">
          {{ authStore.userId === userId ? '收藏功能即将上线' : '根据隐私设置，不可查看' }}
        </p>
      </div>
    </template>

    <!-- 关注/粉丝弹窗 -->
    <Transition name="fade">
      <div v-if="followModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/20" @click.self="closeFollowModal">
        <div class="bg-white rounded-2xl shadow-xl w-96 max-h-[70vh] flex flex-col">
          <div class="flex items-center justify-between p-5 border-b border-gray-100">
            <h3 class="text-sm font-semibold text-text-primary">{{ followModal === 'following' ? '关注列表' : '粉丝列表' }}</h3>
            <button class="text-text-placeholder hover:text-text-secondary transition-colors" @click="closeFollowModal">
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="flex-1 overflow-y-auto p-4 space-y-3">
            <div v-if="followListLoading" class="text-center py-8">
              <svg class="animate-spin w-5 h-5 text-brand mx-auto" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
            </div>
            <div
              v-for="user in followList"
              :key="user.userId || user.id"
              class="flex items-center gap-3 cursor-pointer"
              @click="closeFollowModal(); $router.push(`/user/${user.userId || user.id}`)"
            >
              <div class="w-9 h-9 rounded-full bg-gradient-to-br from-brand/20 to-brand-light/30 flex items-center justify-center text-xs font-bold text-brand">
                {{ user.nickname?.charAt(0) || 'U' }}
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-text-primary truncate">{{ user.nickname }}</p>
                <p v-if="user.bio" class="text-xs text-text-secondary truncate">{{ user.bio }}</p>
              </div>
            </div>
            <div v-if="!followListLoading && followList.length === 0" class="text-center py-4">
              <p class="text-sm text-text-placeholder">暂无数据</p>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
