<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserProfile, getFollowStatus, followUser, unfollowUser, getFollowingList, getFollowerList } from '@/api/users'
import { getUserArticles } from '@/api/articles'
import { getPinList } from '@/api/pins'
import { getMyFolders, createFolder, updateFolder, deleteFolder } from '@/api/favorites'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { UserProfileVO, ArticleVO, PinVO, FollowUserVO, FavoriteFolderVO } from '@/types'
import FavoriteEditModal from '@/components/FavoriteEditModal.vue'
import UserFollowTab from '@/components/UserFollowTab.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const userId = computed(() => Number(route.params.id))
const isCurrentUser = computed(() => authStore.userId === userId.value)

// 资料
const profile = ref<UserProfileVO | null>(null)
const followStatus = ref({ isFollowing: false, isFollowedBy: false })
const loading = ref(true)
const error = ref('')

// Tab 切换
// 动态(activities)、文章(articles)、沸点(pins)、收藏集(favorites)、关注(following)、赞(likes)
type TabKey = 'activities' | 'articles' | 'pins' | 'favorites' | 'following' | 'likes'
const activeTab = ref<TabKey>('activities')

// 根据 URL query 参数初始化 Tab
const tabFromQuery = route.query.tab as string
if (tabFromQuery && ['activities', 'articles', 'pins', 'favorites', 'following', 'likes'].includes(tabFromQuery)) {
  activeTab.value = tabFromQuery as TabKey
}

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

// 过滤后的文章列表（只显示审核通过的）
const filteredArticles = computed(() => {
  return articles.value.filter(article => article.status === 2)
})

// 收藏集相关
const folders = ref<FavoriteFolderVO[]>([])
const foldersLoading = ref(false)
const editModalVisible = ref(false)
const editingFolder = ref<FavoriteFolderVO | null>(null)
const { isSubmitting: folderSubmitting, withLock: withFolderLock } = useSubmitLock()

/**
 * 加载收藏集列表
 */
async function loadFolders() {
  if (foldersLoading.value) return
  foldersLoading.value = true
  try {
    folders.value = await getMyFolders()
  } catch {
    folders.value = []
  } finally {
    foldersLoading.value = false
  }
}

/**
 * 打开新建收藏集弹窗
 */
function openCreateFolder() {
  editingFolder.value = null
  editModalVisible.value = true
}

/**
 * 打开编辑收藏集弹窗
 * @param folder - 要编辑的收藏集对象
 */
function openEditFolder(folder: FavoriteFolderVO) {
  editingFolder.value = folder
  editModalVisible.value = true
}

/**
 * 处理收藏集创建/编辑确认
 * @param data - 表单数据（名称、描述、可见性）
 */
async function handleFolderConfirm(data: { name: string; description: string; isPublic: boolean }) {
  await withFolderLock(async () => {
    try {
      if (editingFolder.value) {
        // 编辑模式
        await updateFolder(editingFolder.value.id, data)
        const idx = folders.value.findIndex(f => f.id === editingFolder.value!.id)
        if (idx !== -1) {
          folders.value[idx] = { ...folders.value[idx], ...data }
        }
        toast.success('收藏集已更新')
      } else {
        // 新建模式
        const newFolder = await createFolder(data)
        folders.value.push(newFolder)
        toast.success('收藏集已创建')
      }
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '操作失败')
    }
  })
}

/**
 * 删除收藏集（默认收藏集不可删除）
 * @param folder - 要删除的收藏集对象
 */
function handleDeleteFolder(folder: FavoriteFolderVO) {
  if (folder.isDefault) {
    toast.error('默认收藏集不可删除')
    return
  }
  if (!confirm(`确定删除「${folder.name}」吗？`)) return
  deleteFolder(folder.id).then(() => {
    folders.value = folders.value.filter(f => f.id !== folder.id)
    toast.success('已删除')
  }).catch((e: unknown) => {
    toast.error(e instanceof Error ? e.message : '删除失败')
  })
}

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
    } else if (activeTab.value === 'favorites' && isCurrentUser.value) {
      // 收藏集仅在查看自己的主页时加载
      await loadFolders()
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

function goToSettings() {
  router.push('/settings')
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
  { key: 'activities', label: '动态' },
  { key: 'articles', label: '文章' },
  { key: 'pins', label: '沸点' },
  { key: 'favorites', label: '收藏集' },
  { key: 'following', label: '关注' },
  { key: 'likes', label: '赞' },
]
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-6">
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
      <div class="flex gap-6">
        <!-- 左侧主内容区 -->
        <div class="flex-1 min-w-0">
          <!-- 用户信息卡 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-6 mb-6">
            <div class="flex items-start gap-5">
              <!-- 头像 -->
              <div class="w-16 h-16 rounded-full bg-gray-100 flex-shrink-0 overflow-hidden">
                <img v-if="profile.avatar" :src="profile.avatar" :alt="profile.nickname" class="w-full h-full object-cover" />
                <span v-else class="w-full h-full flex items-center justify-center text-xl font-bold text-gray-400">{{ profile.nickname?.charAt(0) || 'U' }}</span>
              </div>

              <div class="flex-1 min-w-0">
                <!-- 用户名 -->
                <h1 class="text-xl font-bold text-text-primary">{{ profile.nickname }}</h1>

                <!-- 等级标识 -->
                <div class="mt-1.5">
                  <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-medium bg-purple-50 text-purple-600">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                    </svg>
                    JY.{{ profile.level }}
                  </span>
                </div>
              </div>

              <!-- 右侧社交链接 + 设置/关注按钮 -->
              <div class="flex items-center gap-3 flex-shrink-0">
                <!-- 社交链接图标 -->
                <div class="flex items-center gap-2 mr-2">
                  <a
                    v-for="link in profile.socialLinks?.slice(0, 3)"
                    :key="link.linkType"
                    :href="link.linkUrl"
                    target="_blank"
                    class="w-8 h-8 rounded-full bg-gray-50 hover:bg-gray-100 flex items-center justify-center text-text-secondary hover:text-text-primary transition-colors"
                    @click.stop
                  >
                    <!-- GitHub -->
                    <svg v-if="link.linkType === 'github'" class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                    </svg>
                    <!-- Weibo -->
                    <svg v-else-if="link.linkType === 'weibo'" class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M10.098 20.323c-3.977.391-7.414-1.406-7.672-4.02-.259-2.609 2.759-5.047 6.74-5.441 3.979-.394 7.413 1.404 7.671 4.018.259 2.6-2.759 5.049-6.737 5.439l-.002.004zM9.05 17.219c-.384.616-1.208.884-1.829.602-.612-.279-.793-.991-.406-1.593.379-.595 1.176-.861 1.793-.601.622.263.82.972.442 1.592zm1.27-1.627c-.141.237-.449.353-.689.253-.236-.09-.313-.361-.177-.586.138-.227.436-.346.672-.24.239.09.315.36.18.573h.014zm.176-2.719c-1.893-.493-4.033.45-4.857 2.118-.836 1.704-.026 3.591 1.886 4.21 1.983.64 4.318-.341 5.132-2.179.8-1.793-.201-3.642-2.161-4.149zm7.563-1.224c-.346-.105-.579-.18-.405-.649.381-1.025.422-1.909.003-2.54-.786-1.19-2.937-1.127-5.399-.034 0 0-.774.34-.576-.277.381-1.217.324-2.234-.27-2.822-1.348-1.335-4.918.045-7.973 3.084C1.134 10.611 0 12.784 0 14.667c0 3.604 4.635 5.799 9.165 5.799 5.936 0 9.885-3.446 9.885-6.185 0-1.653-1.397-2.591-2.991-3.632zm1.752-6.191c-.746-.855-1.836-1.185-2.81-1.088-.369.037-.615.375-.553.737.06.362.394.61.764.572.564-.056 1.173.149 1.578.612.406.464.531 1.086.365 1.654-.096.348.108.71.457.807.35.098.713-.106.81-.454.26-.95.039-2.035-.611-2.84zm2.385-2.094C20.488.768 18.755.09 17.006.261c-.369.039-.614.378-.554.74.06.361.393.608.762.569 1.305-.136 2.62.383 3.488 1.374.869.992 1.133 2.316.79 3.51-.096.348.109.71.458.806.349.098.712-.106.809-.453.455-1.611.096-3.439-1.089-4.801z"/>
                    </svg>
                    <!-- Blog / 默认 -->
                    <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"/>
                    </svg>
                  </a>
                </div>

                <!-- 设置按钮（仅自己可见） -->
                <button
                  v-if="isCurrentUser"
                  class="px-5 py-1.5 rounded text-sm font-medium border border-gray-300 text-text-secondary hover:text-brand hover:border-brand transition-all duration-200"
                  @click="goToSettings"
                >
                  设置
                </button>

                <!-- 关注按钮（他人主页） -->
                <button
                  v-else-if="authStore.userId && authStore.userId !== userId"
                  class="px-5 py-1.5 rounded text-sm font-medium transition-all duration-200"
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

          <!-- Tab 切换 -->
          <div class="flex items-center gap-6 mb-6 border-b border-gray-100/80">
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

          <!-- 动态列表 -->
          <div v-if="activeTab === 'activities'" class="space-y-3">
            <div v-if="articles.length > 0" class="space-y-3">
              <div
                v-for="article in articles.slice(0, 5)"
                :key="article.id"
                class="bg-white rounded-xl p-4 shadow-[0_1px_3px_rgba(0,0,0,0.04)]"
              >
                <p class="text-xs text-text-secondary mb-2">{{ profile.nickname }} 赞了这篇文章</p>
                <article
                  class="flex gap-4 cursor-pointer hover:bg-gray-50/50 rounded-lg transition-colors"
                  @click="$router.push(`/article/${article.id}`)"
                >
                  <div v-if="article.coverImage" class="w-28 h-20 rounded-lg overflow-hidden flex-shrink-0">
                    <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" loading="lazy" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="text-sm font-semibold text-text-primary line-clamp-1 hover:text-brand transition-colors">{{ article.title }}</h3>
                    <p class="text-xs text-text-secondary line-clamp-1 mt-1">{{ article.summary }}</p>
                    <div class="flex items-center gap-3 text-xs text-text-secondary/70 mt-2">
                      <span class="flex items-center gap-0.5">
                        <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="2"/><path d="M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5z"/></svg>
                        {{ formatCount(article.viewCount) }}
                      </span>
                      <span class="flex items-center gap-0.5">
                        <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                        {{ formatCount(article.likeCount) }}
                      </span>
                    </div>
                  </div>
                </article>
              </div>
            </div>
            <div v-else class="text-center py-12">
              <p class="text-sm text-text-placeholder">暂无动态</p>
            </div>
          </div>

          <!-- 文章列表 -->
          <div v-else-if="activeTab === 'articles'" class="space-y-0">
            <article
              v-for="(article, idx) in filteredArticles"
              :key="article.id"
              class="bg-white px-5 py-4 cursor-pointer border-b border-gray-50/80
                     hover:bg-gray-50/30 transition-colors duration-150 group"
              :class="{ 'border-t border-gray-100/60': idx === 0 }"
              @click="$router.push(`/article/${article.id}`)"
            >
              <div class="flex gap-5">
                <!-- 文字区 -->
                <div class="flex-1 min-w-0 flex flex-col justify-between">
                  <div>
                    <!-- 标题 -->
                    <h3 class="text-base font-semibold text-text-primary leading-snug mb-1.5
                               group-hover:text-brand transition-colors duration-200 line-clamp-2">
                      {{ article.title }}
                    </h3>
                    <!-- 摘要 -->
                    <p class="text-sm text-text-secondary/75 leading-relaxed mb-2 line-clamp-2">
                      {{ article.summary }}
                    </p>
                  </div>
                  <!-- 底部元信息 -->
                  <div class="flex items-center gap-3 text-xs text-text-placeholder flex-wrap">
                    <span>{{ profile?.nickname }}</span>
                    <span class="flex items-center gap-1">
                      <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                      {{ formatCount(article.likeCount) }}
                    </span>
                    <span class="flex items-center gap-1">
                      <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                      {{ formatCount(article.commentCount) }}
                    </span>
                    <span v-if="article.publishTime" class="text-text-placeholder/70">
                      {{ timeAgo(article.publishTime) }}
                    </span>
                    <!-- 标签 -->
                    <span v-if="article.tags?.length" class="flex items-center gap-1.5">
                      <span
                        v-for="tag in article.tags.slice(0, 2)"
                        :key="tag.id"
                        class="px-2 py-0.5 rounded-full text-[11px] bg-brand/5 text-brand/80 hover:bg-brand/10 transition-colors cursor-pointer"
                        @click.stop="$router.push(`/categories/${tag.id}`)"
                      >
                        {{ tag.name }}
                      </span>
                    </span>
                  </div>
                </div>
                <!-- 封面图 -->
                <img
                  v-if="article.coverImage"
                  :src="article.coverImage"
                  :alt="article.title"
                  class="w-28 h-20 rounded-lg object-cover flex-shrink-0
                         group-hover:opacity-90 transition-opacity duration-300"
                  loading="lazy"
                />
              </div>
            </article>

            <div v-if="filteredArticles.length === 0 && !listLoading" class="text-center py-12">
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

          <!-- 收藏集 -->
          <div v-else-if="activeTab === 'favorites'">
            <!-- 非本人：提示隐私设置 -->
            <div v-if="!isCurrentUser" class="text-center py-12">
              <p class="text-sm text-text-placeholder">根据隐私设置，不可查看</p>
            </div>

            <!-- 本人：收藏集列表 -->
            <template v-else>
              <!-- 头部：新建按钮 -->
              <div v-if="isCurrentUser" class="flex justify-end mb-4">
                <button
                  class="flex items-center gap-1 text-sm font-medium text-brand hover:text-brand-dark transition-colors"
                  @click="openCreateFolder"
                >
                  <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 5v14M5 12h14"/>
                  </svg>
                  新建收藏集
                </button>
              </div>

              <!-- 加载中 -->
              <div v-if="foldersLoading && folders.length === 0" class="text-center py-12">
                <svg class="animate-spin w-8 h-8 text-brand mx-auto" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
              </div>

              <!-- 收藏集列表 -->
              <div v-else-if="folders.length > 0" class="space-y-2">
                <div
                  v-for="folder in folders"
                  :key="folder.id"
                  class="group relative bg-white rounded-xl p-4 shadow-[0_1px_3px_rgba(0,0,0,0.04)]
                     hover:shadow-[0_2px_8px_rgba(0,0,0,0.08)] cursor-pointer transition-all duration-200"
                  @click="$router.push(`/favorites/${folder.id}`)"
                >
                  <div class="flex items-start justify-between">
                    <!-- 左侧信息区 -->
                    <div class="min-w-0 flex-1 pr-16">
                      <!-- 名称行：名称 + 锁图标 + 默认标签 -->
                      <div class="flex items-center gap-1.5 mb-1">
                        <span class="text-sm font-bold text-text-primary truncate">{{ folder.name }}</span>
                        <!-- 隐私锁图标 -->
                        <svg v-if="!folder.isPublic" class="w-3.5 h-3.5 text-text-placeholder flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                          <path d="M7 11V7a5 5 0 0110 0v4"/>
                        </svg>
                        <!-- 默认标签 -->
                        <span
                          v-if="folder.isDefault"
                          class="flex-shrink-0 px-1.5 py-0.5 rounded text-[10px] font-medium bg-gray-100 text-text-secondary"
                        >
                          默认
                        </span>
                      </div>
                      <!-- 副标题：文章数 -->
                      <p class="text-xs text-text-secondary">
                        {{ folder.articleCount }}篇文章
                      </p>
                    </div>

                    <!-- 右侧操作按钮（hover 显示，仅非默认收藏集显示） -->
                    <div v-if="isCurrentUser && !folder.isDefault" class="absolute right-4 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 flex items-center gap-1 transition-opacity">
                      <!-- 编辑按钮 -->
                      <button
                        class="flex items-center gap-1 px-2 py-1 rounded text-xs text-text-secondary hover:text-brand hover:bg-brand/5 transition-colors"
                        @click.stop="openEditFolder(folder)"
                      >
                        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                          <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                        编辑
                      </button>
                      <!-- 删除按钮 -->
                      <button
                        class="flex items-center gap-1 px-2 py-1 rounded text-xs text-text-secondary hover:text-red-500 hover:bg-red-50 transition-colors"
                        @click.stop="handleDeleteFolder(folder)"
                      >
                        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                        </svg>
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 空状态 -->
              <div v-else class="text-center py-12">
                <p class="text-sm text-text-placeholder">暂无收藏集，点击上方按钮创建</p>
              </div>
            </template>
          </div>

          <!-- 关注 -->
          <div v-else-if="activeTab === 'following'">
            <UserFollowTab :user-id="userId" />
          </div>

          <!-- 赞 -->
          <div v-else-if="activeTab === 'likes'" class="text-center py-12">
            <p class="text-sm text-text-placeholder">暂无点赞记录</p>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="w-72 flex-shrink-0 space-y-4 hidden lg:block">
          <!-- 关注/粉丝统计 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <div class="grid grid-cols-2 gap-4 text-center">
              <div class="cursor-pointer group py-2" @click="openFollowModal('following')">
                <p class="text-sm text-text-secondary">关注了</p>
                <p class="text-xl font-bold text-text-primary group-hover:text-brand transition-colors mt-1">{{ profile.followingCount }}</p>
              </div>
              <div class="cursor-pointer group py-2" @click="openFollowModal('followers')">
                <p class="text-sm text-text-secondary">关注者</p>
                <p class="text-xl font-bold text-text-primary group-hover:text-brand transition-colors mt-1">{{ profile.followerCount }}</p>
              </div>
            </div>
          </div>

          <!-- 其他统计 -->
          <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5 space-y-1">
            <div
              class="flex items-center justify-between py-3 px-2 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
              @click="activeTab = 'favorites'"
            >
              <span class="text-base text-text-secondary">收藏集</span>
              <span class="text-base font-medium text-text-primary">2</span>
            </div>
            <div
              class="flex items-center justify-between py-3 px-2 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
              @click="activeTab = 'following'"
            >
              <span class="text-base text-text-secondary">关注标签</span>
              <span class="text-base font-medium text-text-primary">{{ profile.tags?.length || 0 }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-2">
              <span class="text-base text-text-secondary">加入于</span>
              <span class="text-base font-medium text-text-primary">{{ profile.createTime ? new Date(profile.createTime).toLocaleDateString('zh-CN') : '-' }}</span>
            </div>
          </div>
        </div>
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

    <!-- 收藏集编辑弹窗 -->
    <FavoriteEditModal
      :visible="editModalVisible"
      :folder="editingFolder"
      @confirm="handleFolderConfirm"
      @cancel="editModalVisible = false"
      @update:visible="(val: boolean) => editModalVisible = val"
    />
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
