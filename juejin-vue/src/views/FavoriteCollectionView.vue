<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFolderDetail, getFolderArticles } from '@/api/favorites'
import { getUserProfile } from '@/api/users'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import type { FavoriteFolderVO, FavoriteRecordVO, UserProfileVO } from '@/types'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const authStore = useAuthStore()

// 收藏集ID
const folderId = computed(() => Number(route.params.id))

// 数据状态
const folder = ref<FavoriteFolderVO | null>(null)
const ownerProfile = ref<UserProfileVO | null>(null)
const articles = ref<FavoriteRecordVO[]>([])
const loading = ref(true)
const error = ref('')

// 分页相关
const page = ref(1)
const total = ref(0)
const pageSize = 10

/**
 * 格式化数字，大于等于10000显示为xk
 * @param n - 要格式化的数字
 * @returns 格式化后的字符串
 */
function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

/**
 * 计算相对时间（如"3小时前"）
 * @param d - 时间字符串
 * @returns 相对时间描述
 */
function timeAgo(d: string): string {
  const diff = Date.now() - new Date(d).getTime()
  const h = Math.floor(diff / 3600000)
  if (h < 24) return h <= 0 ? '刚刚' : `${h}小时前`
  const days = Math.floor(h / 24)
  if (days < 30) return `${days}天前`
  return new Date(d).toLocaleDateString('zh-CN')
}

/**
 * 加载收藏集详情和文章列表
 */
async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    // 并行加载收藏集详情和文章列表
    const [folderData, articleData] = await Promise.all([
      getFolderDetail(folderId.value),
      getFolderArticles(folderId.value, { page: page.value, size: pageSize }),
    ])
    folder.value = folderData
    articles.value = articleData.list
    total.value = articleData.total

    // 加载收藏集作者信息（如果有的话）
    try {
      ownerProfile.value = await getUserProfile(authStore.userId || folderData.id)
    } catch {
      // API失败时使用当前登录用户信息或默认值
      if (authStore.user) {
        ownerProfile.value = {
          id: authStore.userId!,
          nickname: authStore.user.nickname,
          avatar: authStore.user.avatar,
          bio: authStore.user.bio,
          backgroundImage: '',
          level: authStore.user.level,
          points: authStore.user.points,
          followingCount: 0,
          followerCount: 0,
          articleCount: 0,
          likeCount: 0,
          totalViewCount: 0,
          title: '',
          tags: [],
          socialLinks: [],
          privacy: { showFavorites: true, showFollowing: true, showFollowers: true, allowStrangerMessage: false, messagePushEnabled: true },
          badges: [],
        }
      }
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '收藏集不存在或加载失败'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

/**
 * 加载更多文章（滚动加载）
 */
async function loadMore() {
  if (articles.value.length >= total.value) return
  page.value++
  try {
    const data = await getFolderArticles(folderId.value, { page: page.value, size: pageSize })
    articles.value.push(...data.list)
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '加载更多失败')
  }
}

/**
 * 导航到文章详情
 * @param articleId - 文章ID
 */
function goToArticle(articleId: number) {
  router.push(`/article/${articleId}`)
}

/**
 * 导航到用户主页
 * @param userId - 用户ID
 */
function goToUser(userId: number) {
  router.push(`/user/${userId}`)
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="min-h-screen bg-bg">
    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center items-center py-32">
      <svg class="animate-spin w-10 h-10 text-brand" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error || !folder" class="text-center py-32">
      <p class="text-text-secondary text-sm">{{ error || '收藏集不存在' }}</p>
      <button class="mt-4 text-brand text-sm hover:underline" @click="$router.back()">返回</button>
    </div>

    <!-- 主内容 -->
    <template v-else>
      <!-- 蓝色渐变头部区域 -->
      <div class="relative bg-gradient-to-br from-[#4169E1] to-[#1E80FF] pt-12 pb-8 px-6">
        <!-- 分享按钮（右上角） -->
        <button
          class="absolute top-4 right-6 flex items-center gap-1 px-3 py-1.5 rounded-full bg-white/15 hover:bg-white/25 text-white/90 text-xs font-medium transition-colors"
          title="分享"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
            <path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"/>
          </svg>
          分享
        </button>

        <div class="max-w-4xl mx-auto">
          <!-- 收藏集名称 -->
          <h1 class="text-2xl font-bold text-white mb-4">{{ folder.name }}</h1>

          <!-- 用户信息 + 更多收藏集链接 -->
          <div class="flex items-center gap-3">
            <!-- 头像 -->
            <div class="w-9 h-9 rounded-full bg-white/20 overflow-hidden flex-shrink-0 cursor-pointer" @click="ownerProfile && goToUser(ownerProfile.id)">
              <img
                v-if="ownerProfile?.avatar"
                :src="ownerProfile.avatar"
                :alt="ownerProfile.nickname"
                class="w-full h-full object-cover"
              />
              <span v-else class="w-full h-full flex items-center justify-center text-sm font-bold text-white">
                {{ ownerProfile?.nickname?.charAt(0) || 'U' }}
              </span>
            </div>

            <!-- 昵称 -->
            <span
              v-if="ownerProfile"
              class="text-sm text-white/85 cursor-pointer hover:text-white transition-colors"
              @click="goToUser(ownerProfile.id)"
            >
              {{ ownerProfile.nickname }}
            </span>

            <!-- 更多收藏集链接 -->
            <router-link
              v-if="ownerProfile"
              :to="{ path: `/user/${ownerProfile.id}`, query: { tab: 'favorites' } }"
              class="flex items-center gap-0.5 text-sm text-white/70 hover:text-white transition-colors ml-2"
            >
              更多收藏集
              <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 18l6-6-6-6"/>
              </svg>
            </router-link>
          </div>
        </div>
      </div>

      <!-- 统计栏 -->
      <div class="bg-white border-b border-gray-100">
        <div class="max-w-4xl mx-auto px-6 py-3 flex items-center gap-6 text-sm text-text-secondary">
          <span>{{ folder.articleCount }}篇文章</span>
          <span v-if="!folder.isPublic" class="flex items-center gap-1 text-text-placeholder">
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0110 0v4"/>
            </svg>
            私密
          </span>
        </div>
      </div>

      <!-- 文章列表区域 -->
      <div class="max-w-4xl mx-auto px-4 py-6">
        <!-- 文章卡片列表 -->
        <div v-if="articles.length > 0" class="space-y-4">
          <article
            v-for="article in articles"
            :key="article.articleId"
            class="group bg-white rounded-xl p-5 shadow-[0_1px_3px_rgba(0,0,0,0.04)]
                   hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)] hover:-translate-y-0.5
                   cursor-pointer transition-all duration-300"
            @click="goToArticle(article.articleId)"
          >
            <div class="flex gap-5">
              <!-- 左侧：文字内容区 -->
              <div class="flex-1 min-w-0 flex flex-col justify-between">
                <!-- 标题 -->
                <h3 class="text-base font-bold text-text-primary line-clamp-2 group-hover:text-brand transition-colors leading-snug">
                  {{ article.title }}
                </h3>

                <!-- 摘要 -->
                <p class="text-sm text-text-secondary line-clamp-2 mt-2 leading-relaxed">
                  {{ article.summary }}
                </p>

                <!-- 底部信息：作者、时间、互动数据、标签 -->
                <div class="mt-3 flex items-center flex-wrap gap-x-4 gap-y-1.5 text-xs text-text-secondary/70">
                  <!-- 作者 -->
                  <span class="flex items-center gap-1 cursor-pointer hover:text-brand transition-colors" @click.stop="() => {}">
                    {{ article.authorNickname }}
                  </span>
                  <!-- 时间 -->
                  <span>{{ timeAgo(article.favoriteTime) }}</span>
                  <!-- 浏览量 -->
                  <span class="flex items-center gap-0.5">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="2"/><path d="M12 4.5C7 4.5 2.7 7.6 1 12c1.7 4.4 6 7.5 11 7.5s9.3-3.1 11-7.5c-1.7-4.4-6-7.5-11-7.5z"/>
                    </svg>
                    {{ formatCount(article.viewCount) }}
                  </span>
                  <!-- 点赞数 -->
                  <span class="flex items-center gap-0.5">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/>
                    </svg>
                    {{ article.likeCount }}
                  </span>
                  <!-- 评论数 -->
                  <span class="flex items-center gap-0.5">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    {{ article.commentCount }}
                  </span>
                </div>

                <!-- 标签 -->
                <div v-if="article.tags?.length" class="mt-2 flex items-center gap-1.5 flex-wrap">
                  <span
                    v-for="tag in article.tags"
                    :key="tag.id"
                    class="px-2 py-0.5 rounded text-[11px] bg-gray-50 text-text-secondary"
                  >
                    {{ tag.name }}
                  </span>
                </div>
              </div>

              <!-- 右侧：封面图 -->
              <div
                v-if="article.coverImage"
                class="w-36 h-24 rounded-lg overflow-hidden flex-shrink-0"
              >
                <img
                  :src="article.coverImage"
                  :alt="article.title"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  loading="lazy"
                />
              </div>
            </div>
          </article>
        </div>

        <!-- 空状态 -->
        <div v-else class="text-center py-20">
          <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-gray-50 flex items-center justify-center">
            <svg class="w-10 h-10 text-text-placeholder" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <p class="text-sm text-text-placeholder">暂无收藏的文章</p>
        </div>

        <!-- 加载更多按钮 -->
        <div v-if="articles.length > 0 && articles.length < total" class="text-center mt-6">
          <button
            class="px-6 py-2 rounded-lg text-sm text-text-secondary border border-gray-200 hover:border-brand hover:text-brand transition-colors"
            @click="loadMore"
          >
            加载更多
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
