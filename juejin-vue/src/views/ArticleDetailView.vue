<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticle } from '@/api/articles'
import { createComment, getCommentList } from '@/api/comments'
import { toggleLike } from '@/api/likes'
import { addFavorite, removeFavorite } from '@/api/favorites'
import { getUserProfile, getFollowStatus, followUser, unfollowUser } from '@/api/users'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import type { ArticleVO, CommentVO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const articleId = computed(() => Number(route.params.id))

// 文章数据
const article = ref<ArticleVO | null>(null)
const loading = ref(true)
const error = ref('')

// 目录
interface TocItem { id: string; text: string; level: number }
const tocItems = ref<TocItem[]>([])
const activeTocId = ref('')

// 评论区
const comments = ref<CommentVO[]>([])
const commentPage = ref(1)
const commentTotal = ref(0)
const commentLoading = ref(false)
const commentSort = ref<'latest' | 'hot'>('latest')

// 新评论
const newComment = ref('')
const submittingComment = ref(false)

// 点赞/收藏状态
const liked = ref(false)
const favorited = ref(false)

// 回复
const replyTarget = ref<{ id: number; userId: number; nickname: string } | null>(null)
const replyContent = ref('')

// 作者信息
const authorProfile = ref<any>(null)
const followStatus = ref({ isFollowing: false, isFollowedBy: false })

// 加载文章
async function loadArticle() {
  loading.value = true
  error.value = ''
  try {
    const data = await getArticle(articleId.value)
    article.value = data
    liked.value = data.liked ?? false
    favorited.value = data.favorited ?? false

    // 生成目录
    generateToc(data.contentHtml || data.content || '')

    // 加载作者信息
    loadAuthor(data.authorId)
  } catch {
    error.value = '文章加载失败'
  } finally {
    loading.value = false
  }
}

// 从 HTML 中提取标题生成目录
function generateToc(html: string) {
  const items: TocItem[] = []
  const regex = /<h([1-3])\s+id="([^"]*)"[^>]*>(.*?)<\/h[1-3]>/gi
  let match
  while ((match = regex.exec(html)) !== null) {
    items.push({ level: Number(match[1]), id: match[2], text: match[3].replace(/<[^>]+>/g, '') })
  }
  tocItems.value = items
}

// 加载作者信息
async function loadAuthor(userId: number) {
  try {
    const [profile, follow] = await Promise.all([
      getUserProfile(userId),
      authStore.isLoggedIn ? getFollowStatus(userId) : Promise.resolve(null),
    ])
    authorProfile.value = profile
    if (follow) followStatus.value = follow
  } catch {
    // 作者信息非关键功能，静默处理
  }
}

// 点赞
async function handleLike() {
  if (!authStore.isLoggedIn) { redirectLogin(); return }
  liked.value = !liked.value
  if (article.value) article.value.likeCount += liked.value ? 1 : -1
  try {
    await toggleLike(articleId.value, 1)
  } catch (e: unknown) {
    // 回滚
    liked.value = !liked.value
    if (article.value) article.value.likeCount += liked.value ? 1 : -1
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

// 收藏
async function handleFavorite() {
  if (!authStore.isLoggedIn) { redirectLogin(); return }
  const wasFav = favorited.value
  favorited.value = !favorited.value
  if (article.value) article.value.favoriteCount += favorited.value ? 1 : -1
  try {
    if (wasFav) await removeFavorite(articleId.value)
    else await addFavorite({ articleId: articleId.value })
  } catch (e: unknown) {
    favorited.value = wasFav
    if (article.value) article.value.favoriteCount += favorited.value ? 1 : -1
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

// 关注
async function handleFollow() {
  if (!authStore.isLoggedIn) { redirectLogin(); return }
  const wasFollowing = followStatus.value.isFollowing
  followStatus.value.isFollowing = !wasFollowing
  try {
    if (wasFollowing) await unfollowUser(article.value!.authorId)
    else await followUser(article.value!.authorId)
  } catch (e: unknown) {
    followStatus.value.isFollowing = wasFollowing
    toast.error(e instanceof Error ? e.message : '操作失败')
  }
}

// 加载评论
async function loadComments(reset = false) {
  if (commentLoading.value) return
  if (reset) { commentPage.value = 1; comments.value = [] }
  commentLoading.value = true
  try {
    const data = await getCommentList({
      targetId: articleId.value,
      targetType: 1,
      page: commentPage.value,
      size: 20,
      sortBy: commentSort.value,
    })
    if (reset) {
      comments.value = data.list
    } else {
      comments.value.push(...data.list)
    }
    commentTotal.value = data.total
    commentPage.value++
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '评论加载失败')
  } finally { commentLoading.value = false }
}

// 发表评论
async function handleComment() {
  if (!newComment.value.trim() || submittingComment.value) return
  submittingComment.value = true
  try {
    await createComment({ targetId: articleId.value, targetType: 1, content: newComment.value })
    newComment.value = ''
    loadComments(true)
    if (article.value) article.value.commentCount++
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '评论失败')
  } finally { submittingComment.value = false }
}

// 发表回复
async function handleReply(parentId: number, rootId: number | null, replyUserId: number) {
  if (!replyContent.value.trim()) return
  try {
    await createComment({
      targetId: articleId.value,
      targetType: 1,
      content: replyContent.value,
      parentId,
      rootId: rootId || parentId,
      replyUserId,
    })
    replyContent.value = ''
    replyTarget.value = null
    loadComments(true)
    if (article.value) article.value.commentCount++
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '回复失败')
  }
}

function startReply(comment: CommentVO) {
  replyTarget.value = { id: comment.id, userId: comment.userId, nickname: comment.userNickname }
  replyContent.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

function redirectLogin() {
  router.push({ name: 'login', query: { redirect: route.fullPath } })
}

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

function formatDate(d: string): string {
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// 滚动监听：高亮当前目录项
function onContentScroll() {
  const headings = document.querySelectorAll('h1, h2, h3')
  let current = ''
  headings.forEach(h => {
    const rect = h.getBoundingClientRect()
    if (rect.top < 120) current = h.id
  })
  if (current) activeTocId.value = current
}

onMounted(() => {
  loadArticle()
  loadComments(true)
  document.querySelector('.article-content')?.addEventListener('scroll', onContentScroll)
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- 加载态 -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-20">
      <svg class="animate-spin w-8 h-8 text-brand mb-4" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      <p class="text-text-secondary text-sm">加载中...</p>
    </div>

    <!-- 错误 -->
    <div v-else-if="error" class="flex flex-col items-center justify-center py-20">
      <p class="text-text-secondary text-sm mb-4">{{ error }}</p>
      <button class="text-brand text-sm hover:underline" @click="loadArticle">重新加载</button>
    </div>

    <template v-else-if="article">
      <div class="flex gap-8">
        <!-- 左侧：文章内容 -->
        <div class="flex-1 min-w-0">
          <article class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
            <div class="px-8 py-8">
              <!-- 标题 -->
              <div class="flex items-start gap-3 mb-4">
                <h1 class="text-2xl font-bold text-text-primary leading-snug flex-1">
                  {{ article.title }}
                </h1>
                <!-- 审核状态标签（仅作者可见） -->
                <span
                  v-if="article.authorId === authStore.userId && article.status !== 2"
                  class="flex-shrink-0 px-3 py-1.5 rounded-full text-xs font-medium"
                  :class="{
                    'bg-yellow-50 text-yellow-600 border border-yellow-200': article.status === 1,
                    'bg-red-50 text-red-500 border border-red-200': article.status === 3,
                    'bg-gray-100 text-text-secondary border border-gray-200': article.status === 0,
                  }"
                >
                  {{ { 0: '草稿', 1: '审核中', 3: '已驳回' }[article.status] || '未知状态' }}
                </span>
              </div>

              <!-- 作者信息卡 -->
              <div class="flex items-center gap-4 pb-6 mb-6 border-b border-gray-50">
                <div
                  class="w-10 h-10 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-sm font-bold text-brand cursor-pointer"
                  @click="$router.push(`/user/${article.authorId}`)"
                >
                  {{ article.authorNickname?.charAt(0) || 'U' }}
                </div>
                <div class="flex-1">
                  <router-link :to="`/user/${article.authorId}`" class="text-sm font-semibold text-text-primary hover:text-brand transition-colors">
                    {{ article.authorNickname }}
                  </router-link>
                  <p class="text-xs text-text-secondary mt-0.5">
                    {{ formatDate(article.publishTime || article.createTime) }} · {{ formatCount(article.viewCount) }} 阅读
                  </p>
                </div>
                <button
                  v-if="article.authorId !== authStore.userId"
                  class="px-4 py-1.5 rounded-full text-xs font-medium transition-all duration-200"
                  :class="followStatus.isFollowing
                    ? 'bg-gray-100 text-text-secondary hover:bg-gray-200'
                    : 'bg-brand text-white hover:bg-brand-dark'"
                  @click="handleFollow"
                >
                  {{ followStatus.isFollowing ? '已关注' : '关注' }}
                </button>
              </div>

              <!-- Markdown 正文 -->
              <div
                v-if="article.contentHtml"
                class="article-content prose prose-base max-w-none mb-8"
                v-html="article.contentHtml"
              />
              <div v-else class="article-content whitespace-pre-wrap text-sm leading-relaxed text-text-primary mb-8">
                {{ article.content }}
              </div>

              <!-- 标签 -->
              <div v-if="article.tags?.length" class="flex items-center gap-2 mb-6 flex-wrap">
                <span
                  v-for="tag in article.tags"
                  :key="tag.id"
                  class="px-3 py-1 text-xs rounded-full bg-brand/5 text-brand hover:bg-brand/10 transition-colors cursor-pointer"
                >
                  #{{ tag.name }}
                </span>
              </div>

              <!-- 互动栏 -->
              <div class="flex items-center justify-between py-4 border-t border-gray-50">
                <div class="flex items-center gap-6">
                  <button
                    class="flex items-center gap-1.5 text-sm font-medium transition-all duration-200 cursor-pointer"
                    :class="liked ? 'text-brand' : 'text-text-secondary hover:text-brand'"
                    @click="handleLike"
                  >
                    <svg class="w-4 h-4" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/>
                    </svg>
                    {{ formatCount(article.likeCount) }}
                  </button>

                  <button
                    class="flex items-center gap-1.5 text-sm font-medium transition-all duration-200 cursor-pointer"
                    :class="favorited ? 'text-warm' : 'text-text-secondary hover:text-warm'"
                    @click="handleFavorite"
                  >
                    <svg class="w-4 h-4" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                    </svg>
                    收藏
                  </button>

                  <span class="flex items-center gap-1.5 text-sm text-text-secondary">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    {{ formatCount(article.commentCount) }}
                  </span>
                </div>
              </div>
            </div>
          </article>

          <!-- 评论区 -->
          <div class="mt-6 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-8">
            <h2 class="text-base font-semibold text-text-primary mb-6">
              评论 {{ commentTotal ? `(${commentTotal})` : '' }}
            </h2>

            <!-- 发表评论 -->
            <div v-if="authStore.isLoggedIn" class="flex gap-3 mb-8">
              <div class="w-9 h-9 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0">
                {{ authStore.user?.nickname?.charAt(0) || '我' }}
              </div>
              <div class="flex-1">
                <textarea
                  v-model="newComment"
                  placeholder="写下你的评论..."
                  rows="3"
                  class="w-full p-3 rounded-xl border border-gray-100 text-sm outline-none resize-none
                         focus:border-brand/30 focus:ring-2 focus:ring-brand/5 transition-all duration-200
                         placeholder:text-text-placeholder"
                />
                <button
                  :disabled="!newComment.trim() || submittingComment"
                  class="mt-2 px-5 py-1.5 bg-brand text-white text-xs font-medium rounded-full
                         hover:bg-brand-dark transition-all duration-200
                         disabled:opacity-40 disabled:cursor-not-allowed"
                  @click="handleComment"
                >
                  发表评论
                </button>
              </div>
            </div>
            <div v-else class="mb-8 py-8 text-center bg-gray-50 rounded-xl">
              <p class="text-sm text-text-secondary">
                <span class="text-brand cursor-pointer hover:underline" @click="redirectLogin">登录</span>
                后参与评论
              </p>
            </div>

            <!-- 评论排序 -->
            <div v-if="comments.length" class="flex items-center gap-4 mb-6">
              <button
                class="text-xs font-medium pb-1.5 transition-colors"
                :class="commentSort === 'latest' ? 'text-brand border-b-2 border-brand' : 'text-text-secondary'"
                @click="commentSort = 'latest'; loadComments(true)"
              >
                最新
              </button>
              <button
                class="text-xs font-medium pb-1.5 transition-colors"
                :class="commentSort === 'hot' ? 'text-brand border-b-2 border-brand' : 'text-text-secondary'"
                @click="commentSort = 'hot'; loadComments(true)"
              >
                最热
              </button>
            </div>

            <!-- 评论列表（嵌套） -->
            <div class="space-y-1">
              <div
                v-for="comment in comments"
                :key="comment.id"
                :class="['py-4', { 'border-t border-gray-50': comments.indexOf(comment) > 0 }]"
              >
                <!-- 父评论 -->
                <div class="flex gap-3">
                  <div
                    class="w-8 h-8 rounded-full bg-gradient-to-br from-brand/20 to-brand-light/30 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0 cursor-pointer"
                    @click="$router.push(`/user/${comment.userId}`)"
                  >
                    {{ comment.userNickname?.charAt(0) || 'U' }}
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1.5">
                      <span class="text-sm font-semibold text-text-primary cursor-pointer hover:text-brand" @click="$router.push(`/user/${comment.userId}`)">
                        {{ comment.userNickname }}
                      </span>
                      <span class="text-xs text-text-placeholder">{{ comment.createTime }}</span>
                    </div>
                    <p class="text-sm text-text-primary leading-relaxed mb-2">{{ comment.content }}</p>
                    <div class="flex items-center gap-4 text-xs text-text-secondary">
                      <button
                        class="hover:text-brand transition-colors cursor-pointer"
                        @click="startReply(comment)"
                      >
                        回复
                      </button>
                      <span v-if="comment.replyCount" class="text-text-placeholder">
                        {{ comment.replyCount }} 条回复
                      </span>
                    </div>

                    <!-- 内联回复框 -->
                    <div v-if="replyTarget?.id === comment.id" class="mt-3 flex gap-2">
                      <textarea
                        v-model="replyContent"
                        :placeholder="`回复 @${replyTarget.nickname}...`"
                        rows="2"
                        class="flex-1 p-2 rounded-lg border border-gray-100 text-xs outline-none
                               focus:border-brand/30 focus:ring-1 focus:ring-brand/5 resize-none"
                      />
                      <div class="flex flex-col gap-1">
                        <button
                          class="px-3 py-1 bg-brand text-white text-xs rounded-md hover:bg-brand-dark"
                          @click="handleReply(comment.id, comment.rootId, comment.userId)"
                        >
                          回复
                        </button>
                        <button class="px-3 py-1 bg-gray-100 text-text-secondary text-xs rounded-md" @click="cancelReply">取消</button>
                      </div>
                    </div>

                    <!-- 子回复列表 -->
                    <div v-if="comment.replies?.length" class="mt-3 pl-4 border-l-2 border-gray-50 space-y-3">
                      <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-2">
                        <div
                          class="w-6 h-6 rounded-full bg-gradient-to-br from-brand/15 to-brand-light/25 flex items-center justify-center text-[10px] font-bold text-brand flex-shrink-0"
                        >
                          {{ reply.userNickname?.charAt(0) || 'U' }}
                        </div>
                        <div class="flex-1 min-w-0">
                          <div class="flex items-center gap-2 mb-0.5">
                            <span class="text-xs font-semibold text-text-primary">{{ reply.userNickname }}</span>
                            <span v-if="reply.replyUserId !== comment.userId" class="text-xs text-brand">
                              @{{ reply.replyUserNickname }}
                            </span>
                            <span class="text-xs text-text-placeholder">{{ reply.createTime }}</span>
                          </div>
                          <p class="text-xs text-text-primary leading-relaxed">{{ reply.content }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 加载更多评论 -->
            <div v-if="comments.length < commentTotal" class="text-center py-4">
              <button
                class="text-sm text-brand hover:underline"
                :disabled="commentLoading"
                @click="loadComments()"
              >
                {{ commentLoading ? '加载中...' : '加载更多评论' }}
              </button>
            </div>

            <!-- 暂无评论 -->
            <div v-if="!commentLoading && comments.length === 0" class="py-10 text-center">
              <p class="text-sm text-text-placeholder">暂无评论，来写下第一条吧</p>
            </div>
          </div>
        </div>

        <!-- 右侧目录 -->
        <aside v-if="tocItems.length" class="hidden lg:block w-56 flex-shrink-0">
          <nav class="sticky top-20 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <h4 class="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-3">目录</h4>
            <ul class="space-y-0.5">
              <li
                v-for="item in tocItems"
                :key="item.id"
              >
                <a
                  :href="`#${item.id}`"
                  class="block text-xs py-1.5 transition-all duration-200 truncate"
                  :class="[
                    activeTocId === item.id ? 'text-brand font-medium' : 'text-text-secondary hover:text-text-primary',
                    item.level === 2 ? 'pl-3' : item.level === 3 ? 'pl-6' : '',
                  ]"
                >
                  {{ item.text }}
                </a>
              </li>
            </ul>
          </nav>

          <!-- 作者卡片 -->
          <div v-if="authorProfile" class="mt-4 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
            <div class="flex items-center gap-3 mb-3 cursor-pointer" @click="$router.push(`/user/${article.authorId}`)">
              <div class="w-11 h-11 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-sm font-bold text-brand">
                {{ article.authorNickname?.charAt(0) || 'U' }}
              </div>
              <div>
                <p class="text-sm font-semibold text-text-primary">{{ article.authorNickname }}</p>
                <p class="text-xs text-text-secondary">Lv{{ authorProfile.level }}</p>
              </div>
            </div>
            <div class="grid grid-cols-3 gap-2 text-center">
              <div>
                <p class="text-sm font-bold text-text-primary">{{ formatCount(authorProfile.articleCount || 0) }}</p>
                <p class="text-xs text-text-placeholder">文章</p>
              </div>
              <div>
                <p class="text-sm font-bold text-text-primary">{{ formatCount(authorProfile.followingCount || 0) }}</p>
                <p class="text-xs text-text-placeholder">关注</p>
              </div>
              <div>
                <p class="text-sm font-bold text-text-primary">{{ formatCount(authorProfile.followerCount || 0) }}</p>
                <p class="text-xs text-text-placeholder">粉丝</p>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.article-content :deep(img) {
  border-radius: 0.75rem;
  max-width: 100%;
}

.article-content :deep(pre) {
  border-radius: 0.75rem;
  overflow-x: auto;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  scroll-margin-top: 5rem;
}
</style>
