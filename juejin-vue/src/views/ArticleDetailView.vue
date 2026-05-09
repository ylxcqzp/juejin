<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticle } from '@/api/articles'
import { createComment, getCommentList } from '@/api/comments'
import { toggleLike } from '@/api/likes'
import { addFavorite, removeFavorite } from '@/api/favorites'
import { getUserProfile, getFollowStatus, followUser, unfollowUser } from '@/api/users'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { ArticleVO, CommentVO, UserProfileVO } from '@/types'

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
const { isSubmitting: submittingComment, withLock: withCommentLock } = useSubmitLock()

// 点赞/收藏/关注
const liked = ref(false)
const favorited = ref(false)
const { isSubmitting: liking, withLock: withLikeLock } = useSubmitLock()
const { isSubmitting: favoriting, withLock: withFavLock } = useSubmitLock()
const { isSubmitting: following, withLock: withFollowLock } = useSubmitLock()
const { isSubmitting: replying, withLock: withReplyLock } = useSubmitLock()

// 回复
const replyTarget = ref<{ id: number; userId: number; nickname: string } | null>(null)
const replyContent = ref('')

// 作者信息
const authorProfile = ref<UserProfileVO | null>(null)
const followStatus = ref({ isFollowing: false, isFollowedBy: false })

// 滚动到评论区
const commentsRef = ref<HTMLElement | null>(null)
function scrollToComments() {
  commentsRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 分享
function handleShare() {
  navigator.clipboard.writeText(window.location.href).then(
    () => toast.success('链接已复制'),
    () => toast.error('复制失败'),
  )
}

// 举报
function handleReport() {
  toast.success('举报功能开发中，感谢反馈')
}

// 加载文章
async function loadArticle() {
  loading.value = true
  error.value = ''
  try {
    const data = await getArticle(articleId.value)
    article.value = data
    liked.value = data.liked ?? false
    favorited.value = data.favorited ?? false
    generateToc(data.contentHtml || data.content || '')
    loadAuthor(data.authorId)
  } catch {
    error.value = '文章加载失败'
  } finally {
    loading.value = false
  }
}

// 生成目录
function generateToc(html: string) {
  const items: TocItem[] = []
  const regex = /<h([1-3])\s+id="([^"]*)"[^>]*>(.*?)<\/h[1-3]>/gi
  let match
  while ((match = regex.exec(html)) !== null) {
    items.push({ level: Number(match[1]), id: match[2], text: match[3].replace(/<[^>]+>/g, '') })
  }
  tocItems.value = items
}

// 加载作者
async function loadAuthor(userId: number) {
  try {
    const [profile, follow] = await Promise.all([
      getUserProfile(userId),
      authStore.isLoggedIn ? getFollowStatus(userId) : Promise.resolve(null),
    ])
    authorProfile.value = profile
    if (follow) followStatus.value = follow
  } catch { /* 非关键 */ }
}

// 点赞
async function handleLike() {
  if (!authStore.isLoggedIn) { redirectLogin(); return }
  await withLikeLock(async () => {
    const prev = liked.value
    liked.value = !prev
    if (article.value) article.value.likeCount += liked.value ? 1 : -1
    try { await toggleLike(articleId.value, 1) }
    catch {
      liked.value = prev
      if (article.value) article.value.likeCount += prev ? 1 : -1
      toast.error('操作失败')
    }
  })
}

// 收藏
async function handleFavorite() {
  if (!authStore.isLoggedIn) { redirectLogin(); return }
  await withFavLock(async () => {
    const prev = favorited.value
    favorited.value = !prev
    if (article.value) article.value.favoriteCount += favorited.value ? 1 : -1
    try {
      if (prev) await removeFavorite(articleId.value)
      else await addFavorite({ articleId: articleId.value })
    } catch {
      favorited.value = prev
      if (article.value) article.value.favoriteCount += prev ? 1 : -1
      toast.error('操作失败')
    }
  })
}

// 关注
async function handleFollow() {
  if (!authStore.isLoggedIn) { redirectLogin(); return }
  await withFollowLock(async () => {
    const prev = followStatus.value.isFollowing
    followStatus.value.isFollowing = !prev
    try {
      if (prev) await unfollowUser(article.value!.authorId)
      else await followUser(article.value!.authorId)
    } catch {
      followStatus.value.isFollowing = prev
      toast.error('操作失败')
    }
  })
}

// 加载评论
async function loadComments(reset = false) {
  if (commentLoading.value) return
  if (reset) { commentPage.value = 1; comments.value = [] }
  commentLoading.value = true
  try {
    const data = await getCommentList({
      targetId: articleId.value, targetType: 1,
      page: commentPage.value, size: 20, sortBy: commentSort.value,
    })
    if (reset) comments.value = data.list
    else comments.value.push(...data.list)
    commentTotal.value = data.total
    commentPage.value++
  } catch (e: unknown) {
    toast.error(e instanceof Error ? e.message : '评论加载失败')
  } finally { commentLoading.value = false }
}

// 发表评论
async function handleComment() {
  if (!newComment.value.trim()) return
  await withCommentLock(async () => {
    try {
      await createComment({ targetId: articleId.value, targetType: 1, content: newComment.value })
      newComment.value = ''
      loadComments(true)
      if (article.value) article.value.commentCount++
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : '评论失败') }
  })
}

// 发表回复
async function handleReply(parentId: number, rootId: number | null, replyUserId: number) {
  if (!replyContent.value.trim()) return
  await withReplyLock(async () => {
    try {
      await createComment({ targetId: articleId.value, targetType: 1, content: replyContent.value, parentId, rootId: rootId || parentId, replyUserId })
      replyContent.value = ''
      replyTarget.value = null
      loadComments(true)
      if (article.value) article.value.commentCount++
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : '回复失败') }
  })
}

function startReply(comment: CommentVO) {
  replyTarget.value = { id: comment.id, userId: comment.userId, nickname: comment.userNickname }
  replyContent.value = ''
}
function cancelReply() { replyTarget.value = null; replyContent.value = '' }

function redirectLogin() { router.push({ name: 'login', query: { redirect: route.fullPath } }) }

function formatCount(n: number): string {
  if (n >= 10000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
function formatDate(d: string): string {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}
function formatTime(d: string): string {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// 滚动监听高亮目录
function onContentScroll() {
  const headings = document.querySelectorAll('.article-content h1[id], .article-content h2[id], .article-content h3[id]')
  let current = ''
  headings.forEach(h => { if ((h as HTMLElement).getBoundingClientRect().top < 120) current = h.id })
  if (current) activeTocId.value = current
}

// 目录点击滚动
function scrollToHeading(id: string) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  loadArticle()
  loadComments(true)
  const contentEl = document.querySelector('.article-content')
  if (contentEl) contentEl.addEventListener('scroll', onContentScroll, { passive: true })
})
onUnmounted(() => {
  document.querySelector('.article-content')?.removeEventListener('scroll', onContentScroll)
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- 加载/错误态 -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-20">
      <svg class="animate-spin w-8 h-8 text-brand mb-4" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      <p class="text-text-secondary text-sm">加载中...</p>
    </div>
    <div v-else-if="error" class="flex flex-col items-center justify-center py-20">
      <p class="text-text-secondary text-sm mb-4">{{ error }}</p>
      <button class="text-brand text-sm hover:underline cursor-pointer" @click="loadArticle">重新加载</button>
    </div>

    <template v-else-if="article">
      <div class="flex gap-6">
        <!-- ============================================================ -->
        <!-- 左侧动作栏 (sticky)                                            -->
        <!-- ============================================================ -->
        <aside class="hidden md:flex flex-col items-center gap-1 pt-2 flex-shrink-0" style="width:3.5rem">
          <div class="sticky top-20 flex flex-col items-center gap-4">
            <!-- 点赞 -->
            <button
              class="flex flex-col items-center gap-0.5 cursor-pointer group"
              :class="liking ? 'pointer-events-none opacity-50' : ''"
              @click="handleLike"
            >
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200"
                :class="liked
                  ? 'bg-brand/10 text-brand shadow-[0_0_0_3px_rgba(30,128,255,0.1)]'
                  : 'bg-gray-50 text-text-secondary hover:bg-brand/5 hover:text-brand'"
              >
                <svg class="w-5 h-5" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/>
                </svg>
              </div>
              <span class="text-xs text-text-secondary group-hover:text-brand transition-colors">{{ formatCount(article.likeCount) }}</span>
            </button>

            <!-- 评论 -->
            <button class="flex flex-col items-center gap-0.5 cursor-pointer group" @click="scrollToComments">
              <div class="w-10 h-10 rounded-full bg-gray-50 text-text-secondary flex items-center justify-center
                          group-hover:bg-brand/5 group-hover:text-brand transition-all duration-200">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <span class="text-xs text-text-secondary group-hover:text-brand transition-colors">{{ formatCount(article.commentCount) }}</span>
            </button>

            <!-- 关注 -->
            <button
              v-if="article.authorId !== authStore.userId"
              class="flex flex-col items-center gap-0.5 cursor-pointer group"
              :disabled="following"
              @click="handleFollow"
            >
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200"
                :class="followStatus.isFollowing
                  ? 'bg-brand/10 text-brand'
                  : 'bg-gray-50 text-text-secondary group-hover:bg-brand/5 group-hover:text-brand'"
              >
                <svg class="w-5 h-5" :fill="followStatus.isFollowing ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><path d="M20 8v6M23 11h-6"/>
                </svg>
              </div>
              <span class="text-xs text-text-secondary group-hover:text-brand transition-colors">{{ followStatus.isFollowing ? '已关注' : '关注' }}</span>
            </button>

            <!-- 分享 -->
            <button class="flex flex-col items-center gap-0.5 cursor-pointer group" @click="handleShare">
              <div class="w-10 h-10 rounded-full bg-gray-50 text-text-secondary flex items-center justify-center
                          group-hover:bg-brand/5 group-hover:text-brand transition-all duration-200">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"/>
                </svg>
              </div>
              <span class="text-xs text-text-secondary group-hover:text-brand transition-colors">分享</span>
            </button>

            <!-- 举报 -->
            <button class="flex flex-col items-center gap-0.5 cursor-pointer group" @click="handleReport">
              <div class="w-10 h-10 rounded-full bg-gray-50 text-text-secondary flex items-center justify-center
                          group-hover:bg-red-50 group-hover:text-red-400 transition-all duration-200">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
              </div>
              <span class="text-xs text-text-secondary group-hover:text-red-400 transition-colors">举报</span>
            </button>
          </div>
        </aside>

        <!-- ============================================================ -->
        <!-- 中间：文章内容                                                  -->
        <!-- ============================================================ -->
        <div class="flex-1 min-w-0">
          <article class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden">
            <div class="px-8 py-8">
              <!-- 标题 -->
              <h1 class="text-2xl font-bold text-text-primary leading-snug mb-4">{{ article.title }}</h1>

              <!-- 作者行 -->
              <div class="flex items-center gap-3 pb-6 mb-6 border-b border-gray-50">
                <div
                  class="w-8 h-8 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-xs font-bold text-brand cursor-pointer"
                  @click="$router.push(`/user/${article.authorId}`)"
                >
                  {{ article.authorNickname?.charAt(0) || 'U' }}
                </div>
                <div class="flex items-center gap-2 text-xs text-text-secondary">
                  <span class="font-medium text-text-primary cursor-pointer hover:text-brand transition-colors" @click="$router.push(`/user/${article.authorId}`)">{{ article.authorNickname }}</span>
                  <span class="text-text-placeholder/50">·</span>
                  <span>{{ formatDate(article.publishTime || article.createTime) }}</span>
                  <span class="text-text-placeholder/50">·</span>
                  <span>{{ formatCount(article.viewCount) }} 阅读</span>
                </div>
                <!-- 移动端关注按钮 -->
                <button
                  v-if="article.authorId !== authStore.userId"
                  class="ml-auto md:hidden px-4 py-1.5 rounded-full text-xs font-medium transition-all duration-200"
                  :class="followStatus.isFollowing
                    ? 'bg-gray-100 text-text-secondary hover:bg-gray-200'
                    : 'bg-brand text-white hover:bg-brand-dark'"
                  :disabled="following"
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
              <div v-if="article.tags?.length" class="flex items-center gap-2 mb-4 flex-wrap">
                <span
                  v-for="tag in article.tags" :key="tag.id"
                  class="px-3 py-1 text-xs rounded-full bg-brand/5 text-brand hover:bg-brand/10 transition-colors cursor-pointer"
                  @click="$router.push(`/tags/${tag.id}`)"
                >#{{ tag.name }}</span>
              </div>

              <!-- 底部互动栏 -->
              <div class="flex items-center gap-6 py-4 border-t border-gray-50">
                <button
                  class="flex items-center gap-1.5 text-sm font-medium transition-all duration-200 cursor-pointer"
                  :class="[liked ? 'text-brand' : 'text-text-secondary hover:text-brand', liking ? 'opacity-50' : '']"
                  :disabled="liking" @click="handleLike"
                >
                  <svg class="w-4 h-4" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/></svg>
                  赞 {{ formatCount(article.likeCount) }}
                </button>
                <button
                  class="flex items-center gap-1.5 text-sm font-medium transition-all duration-200 cursor-pointer"
                  :class="[favorited ? 'text-warm' : 'text-text-secondary hover:text-warm', favoriting ? 'opacity-50' : '']"
                  :disabled="favoriting" @click="handleFavorite"
                >
                  <svg class="w-4 h-4" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                  收藏
                </button>
                <button class="flex items-center gap-1.5 text-sm text-text-secondary cursor-pointer" @click="scrollToComments">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  评论 {{ formatCount(article.commentCount) }}
                </button>
              </div>
            </div>
          </article>

          <!-- 评论区 -->
          <div ref="commentsRef" class="mt-4 bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-8">
            <h2 class="text-base font-semibold text-text-primary mb-6">评论 {{ commentTotal ? `(${commentTotal})` : '' }}</h2>

            <!-- 发表评论 -->
            <div v-if="authStore.isLoggedIn" class="flex gap-3 mb-8">
              <div class="w-9 h-9 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0">
                {{ authStore.user?.nickname?.charAt(0) || '我' }}
              </div>
              <div class="flex-1">
                <textarea v-model="newComment" placeholder="写下你的评论..." rows="3"
                  class="w-full p-3 rounded-xl border border-gray-100 text-sm outline-none resize-none
                         focus:border-brand/30 focus:ring-2 focus:ring-brand/5 transition-all duration-200
                         placeholder:text-text-placeholder"
                />
                <button :disabled="!newComment.trim() || submittingComment"
                  class="mt-2 px-5 py-1.5 bg-brand text-white text-xs font-medium rounded-full
                         hover:bg-brand-dark transition-all duration-200
                         disabled:opacity-40 disabled:cursor-not-allowed"
                  @click="handleComment"
                >发表评论</button>
              </div>
            </div>
            <div v-else class="mb-8 py-8 text-center bg-gray-50 rounded-xl">
              <p class="text-sm text-text-secondary">
                <span class="text-brand cursor-pointer hover:underline" @click="redirectLogin">登录</span> 后参与评论
              </p>
            </div>

            <!-- 评论排序 -->
            <div v-if="comments.length" class="flex items-center gap-4 mb-6">
              <button
                v-for="s in [{ k: 'latest' as const, l: '最新' }, { k: 'hot' as const, l: '最热' }]"
                :key="s.k"
                class="text-xs font-medium pb-1.5 transition-colors cursor-pointer"
                :class="commentSort === s.k ? 'text-brand border-b-2 border-brand' : 'text-text-secondary'"
                @click="commentSort = s.k; loadComments(true)"
              >{{ s.l }}</button>
            </div>

            <!-- 评论列表（盖楼模式） -->
            <div class="space-y-1">
              <div
                v-for="comment in comments" :key="comment.id"
                :class="['py-4', { 'border-t border-gray-50': comments.indexOf(comment) > 0 }]"
              >
                <!-- 根评论 -->
                <div class="flex gap-3">
                  <div
                    class="w-8 h-8 rounded-full bg-gradient-to-br from-brand/20 to-brand-light/30 flex items-center justify-center text-xs font-bold text-brand flex-shrink-0 cursor-pointer"
                    @click="$router.push(`/user/${comment.userId}`)"
                  >{{ comment.userNickname?.charAt(0) || 'U' }}</div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1.5">
                      <span class="text-sm font-semibold text-text-primary cursor-pointer hover:text-brand" @click="$router.push(`/user/${comment.userId}`)">{{ comment.userNickname }}</span>
                      <span class="text-xs text-text-placeholder">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <p class="text-sm text-text-primary leading-relaxed mb-2">{{ comment.content }}</p>
                    <div class="flex items-center gap-4 text-xs text-text-secondary">
                      <button class="hover:text-brand transition-colors cursor-pointer" @click="startReply(comment)">回复</button>
                      <span v-if="comment.replyCount" class="text-text-placeholder">{{ comment.replyCount }} 条回复</span>
                    </div>

                    <!-- 根评论回复框 -->
                    <div v-if="replyTarget?.id === comment.id" class="mt-3 flex gap-2">
                      <textarea v-model="replyContent" :placeholder="`回复 @${replyTarget.nickname}...`" rows="2"
                        class="flex-1 p-2 rounded-lg border border-gray-100 text-xs outline-none
                               focus:border-brand/30 focus:ring-1 focus:ring-brand/5 resize-none"
                      />
                      <div class="flex flex-col gap-1">
                        <button
                          class="px-3 py-1 bg-brand text-white text-xs rounded-md hover:bg-brand-dark disabled:opacity-50 disabled:cursor-not-allowed"
                          :disabled="replying" @click="handleReply(comment.id, comment.rootId, comment.userId)"
                        >{{ replying ? '...' : '回复' }}</button>
                        <button class="px-3 py-1 bg-gray-100 text-text-secondary text-xs rounded-md" @click="cancelReply">取消</button>
                      </div>
                    </div>

                    <!-- 子回复（盖楼） -->
                    <div v-if="comment.replies?.length" class="mt-3 pl-4 border-l-2 border-gray-50 space-y-3">
                      <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-2">
                        <div
                          class="w-6 h-6 rounded-full bg-gradient-to-br from-brand/15 to-brand-light/25 flex items-center justify-center text-[10px] font-bold text-brand flex-shrink-0 cursor-pointer"
                          @click="$router.push(`/user/${reply.userId}`)"
                        >{{ reply.userNickname?.charAt(0) || 'U' }}</div>
                        <div class="flex-1 min-w-0">
                          <div class="flex items-center gap-2 mb-0.5">
                            <span class="text-xs font-semibold text-text-primary">{{ reply.userNickname }}</span>
                            <span v-if="reply.parentId !== comment.id || reply.replyUserId !== comment.userId" class="text-xs text-brand">@{{ reply.replyUserNickname }}</span>
                            <span class="text-xs text-text-placeholder">{{ formatTime(reply.createTime) }}</span>
                          </div>
                          <p class="text-xs text-text-primary leading-relaxed mb-1.5">{{ reply.content }}</p>
                          <button class="text-xs text-text-secondary hover:text-brand transition-colors cursor-pointer" @click="startReply(reply)">回复</button>

                          <!-- 子回复回复框 -->
                          <div v-if="replyTarget?.id === reply.id" class="mt-2 flex gap-2">
                            <textarea v-model="replyContent" :placeholder="`回复 @${replyTarget.nickname}...`" rows="2"
                              class="flex-1 p-2 rounded-lg border border-gray-100 text-xs outline-none
                                     focus:border-brand/30 focus:ring-1 focus:ring-brand/5 resize-none"
                            />
                            <div class="flex flex-col gap-1">
                              <button
                                class="px-3 py-1 bg-brand text-white text-xs rounded-md hover:bg-brand-dark disabled:opacity-50 disabled:cursor-not-allowed"
                                :disabled="replying" @click="handleReply(reply.id, reply.rootId, reply.userId)"
                              >{{ replying ? '...' : '回复' }}</button>
                              <button class="px-3 py-1 bg-gray-100 text-text-secondary text-xs rounded-md" @click="cancelReply">取消</button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 加载更多 -->
            <div v-if="comments.length < commentTotal" class="text-center py-4">
              <button class="text-sm text-brand hover:underline cursor-pointer" :disabled="commentLoading" @click="loadComments()">
                {{ commentLoading ? '加载中...' : '加载更多评论' }}
              </button>
            </div>
            <div v-if="!commentLoading && comments.length === 0" class="py-10 text-center">
              <p class="text-sm text-text-placeholder">暂无评论，来写下第一条吧</p>
            </div>
          </div>
        </div>

        <!-- ============================================================ -->
        <!-- 右侧：作者卡片 + 目录                                            -->
        <!-- ============================================================ -->
        <aside class="hidden lg:block w-64 flex-shrink-0">
          <div class="sticky top-20 space-y-4">
            <!-- 作者卡片 -->
            <div v-if="authorProfile" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
              <div class="flex items-center gap-3 mb-3 cursor-pointer" @click="$router.push(`/user/${article.authorId}`)">
                <div class="w-11 h-11 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-sm font-bold text-brand flex-shrink-0">
                  {{ article.authorNickname?.charAt(0) || 'U' }}
                </div>
                <div class="min-w-0">
                  <div class="flex items-center gap-1.5">
                    <p class="text-sm font-semibold text-text-primary truncate">{{ article.authorNickname }}</p>
                    <span class="px-1.5 py-0.5 rounded text-[10px] font-medium bg-warm/10 text-warm flex-shrink-0">Lv{{ authorProfile.level }}</span>
                  </div>
                  <p class="text-xs text-text-secondary truncate mt-0.5">{{ (authorProfile as any).title || authorProfile.bio?.split('|')[0]?.trim() || '技术创作者' }}</p>
                </div>
              </div>
              <div class="grid grid-cols-3 gap-2 py-3 border-y border-gray-50 mb-3">
                <div class="text-center">
                  <p class="text-sm font-bold text-text-primary">{{ formatCount(authorProfile.articleCount || 0) }}</p>
                  <p class="text-[10px] text-text-placeholder mt-0.5">文章</p>
                </div>
                <div class="text-center">
                  <p class="text-sm font-bold text-text-primary">{{ formatCount((authorProfile as any).totalViewCount || 0) }}</p>
                  <p class="text-[10px] text-text-placeholder mt-0.5">阅读</p>
                </div>
                <div class="text-center">
                  <p class="text-sm font-bold text-text-primary">{{ formatCount(authorProfile.followerCount || 0) }}</p>
                  <p class="text-[10px] text-text-placeholder mt-0.5">粉丝</p>
                </div>
              </div>
              <div class="flex gap-2">
                <button
                  v-if="article.authorId !== authStore.userId"
                  class="flex-1 h-9 rounded-full text-xs font-medium transition-all duration-200 cursor-pointer"
                  :class="followStatus.isFollowing
                    ? 'bg-gray-100 text-text-secondary hover:bg-gray-200'
                    : 'bg-brand text-white hover:bg-brand-dark'"
                  :disabled="following"
                  @click="handleFollow"
                >{{ followStatus.isFollowing ? '已关注' : '关注' }}</button>
                <button
                  v-if="article.authorId !== authStore.userId"
                  class="flex-1 h-9 rounded-full border border-gray-200 text-xs font-medium text-text-secondary
                         hover:bg-brand/5 hover:text-brand hover:border-brand/30 transition-all duration-200 cursor-pointer"
                >私信</button>
              </div>
            </div>

            <!-- 目录 -->
            <div v-if="tocItems.length" class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] p-5">
              <h4 class="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-3">目录</h4>
              <ul class="space-y-0.5 max-h-[50vh] overflow-y-auto">
                <li v-for="item in tocItems" :key="item.id">
                  <a
                    :href="`#${item.id}`"
                    class="block text-xs py-1.5 transition-all duration-200 truncate cursor-pointer
                           border-l-2 hover:text-brand hover:border-brand/30"
                    :class="[
                      activeTocId === item.id
                        ? 'text-brand font-medium border-brand'
                        : 'text-text-secondary border-transparent',
                      item.level === 2 ? 'pl-6' : item.level === 3 ? 'pl-9' : 'pl-3',
                    ]"
                    @click.prevent="scrollToHeading(item.id)"
                  >{{ item.text }}</a>
                </li>
              </ul>
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
