<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConversations, getOrCreateConversation, getMessages, sendMessage, markAsRead } from '@/api/messages'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useSubmitLock } from '@/composables/useSubmitLock'
import type { ConversationVO, MessageVO } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// 会话列表
const conversations = ref<ConversationVO[]>([])
const conversationsLoading = ref(false)
const selectedConvId = ref<number | null>(null)
const selectedConv = computed(() => conversations.value.find(c => c.id === selectedConvId.value) || null)

// 消息
const messages = ref<MessageVO[]>([])
const messagesLoading = ref(false)
const newContent = ref('')
const { isSubmitting, withLock } = useSubmitLock()

// 消息容器引用
const messageListRef = ref<HTMLElement | null>(null)

// 加载会话列表
async function loadConversations() {
  conversationsLoading.value = true
  try {
    const data = await getConversations({ page: 1, size: 50 })
    conversations.value = data.list
  } catch {
    toast.error('加载会话列表失败')
  } finally {
    conversationsLoading.value = false
  }
}

// 加载消息
async function loadMessages(convId: number) {
  messagesLoading.value = true
  try {
    const data = await getMessages(convId, { page: 1, size: 100 })
    messages.value = data.list.reverse()
    await nextTick()
    scrollToBottom()
  } catch {
    toast.error('加载消息失败')
  } finally {
    messagesLoading.value = false
  }
}

// 选择会话
async function selectConversation(conv: ConversationVO) {
  selectedConvId.value = conv.id
  router.replace({ name: 'messages', query: { userId: undefined } })
  await loadMessages(conv.id)
  if (conv.unreadCount > 0) {
    try { await markAsRead(conv.id) } catch { /* 非关键 */ }
    conv.unreadCount = 0
  }
}

// 发送消息
async function handleSend() {
  const content = newContent.value.trim()
  if (!content || !selectedConvId.value || !selectedConv.value) return
  await withLock(async () => {
    try {
      const msg = await sendMessage(selectedConvId.value!, {
        receiverId: selectedConv.value!.otherUserId,
        content,
        contentType: 1,
      })
      messages.value.push(msg)
      newContent.value = ''
      await nextTick()
      scrollToBottom()
      // 更新会话列表中的最后消息
      const conv = conversations.value.find(c => c.id === selectedConvId.value)
      if (conv) {
        conv.lastMessage = content.length > 100 ? content.slice(0, 100) : content
        conv.lastMessageTime = new Date().toISOString()
      }
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : '发送失败')
    }
  })
}

// 处理回车发送
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 格式化时间
function formatTime(d: string): string {
  if (!d) return ''
  return new Date(d).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatFullDate(d: string): string {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// 是否显示日期分隔
function showDateSeparator(index: number): boolean {
  if (index === 0) return true
  const prev = new Date(messages.value[index - 1].createTime).toDateString()
  const curr = new Date(messages.value[index].createTime).toDateString()
  return prev !== curr
}

// 初始化：从URL参数获取userId自动创建/打开会话
onMounted(async () => {
  await loadConversations()
  const targetUserId = route.query.userId
  if (targetUserId) {
    try {
      const conv = await getOrCreateConversation(Number(targetUserId))
      // 在列表中查找或添加
      const existing = conversations.value.find(c => c.id === conv.id)
      if (!existing) conversations.value.unshift(conv)
      await selectConversation(conv)
    } catch {
      toast.error('创建会话失败')
    }
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-6">
    <div class="bg-white rounded-2xl shadow-[0_1px_3px_rgba(0,0,0,0.04)] overflow-hidden flex" style="min-height: 70vh">
      <!-- ============================================================ -->
      <!-- 左侧：会话列表                                                  -->
      <!-- ============================================================ -->
      <div class="w-72 flex-shrink-0 border-r border-gray-50 flex flex-col">
        <div class="px-4 py-4 border-b border-gray-50">
          <h2 class="text-base font-semibold text-text-primary">私信</h2>
        </div>
        <div class="flex-1 overflow-y-auto">
          <!-- 加载中 -->
          <div v-if="conversationsLoading" class="flex items-center justify-center py-12">
            <svg class="animate-spin w-5 h-5 text-brand" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
          </div>
          <!-- 空态 -->
          <div v-else-if="!conversations.length" class="flex flex-col items-center justify-center py-12 px-4">
            <svg class="w-10 h-10 text-text-placeholder mb-3" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <p class="text-sm text-text-placeholder">暂无会话</p>
          </div>
          <!-- 会话列表 -->
          <div v-else>
            <div
              v-for="conv in conversations"
              :key="conv.id"
              class="flex items-center gap-3 px-4 py-3 cursor-pointer transition-colors hover:bg-gray-50 border-b border-gray-50/50"
              :class="selectedConvId === conv.id ? 'bg-brand/5 border-l-2 border-l-brand' : ''"
              @click="selectConversation(conv)"
            >
              <div class="relative flex-shrink-0">
                <div class="w-10 h-10 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-xs font-bold text-brand">
                  {{ conv.otherUserNickname?.charAt(0) || 'U' }}
                </div>
                <span v-if="conv.unreadCount > 0"
                  class="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-red-500 text-white text-[10px] font-bold flex items-center justify-center">
                  {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                </span>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-0.5">
                  <p class="text-sm font-medium text-text-primary truncate">{{ conv.otherUserNickname }}</p>
                  <span class="text-[10px] text-text-placeholder flex-shrink-0 ml-1">{{ formatTime(conv.lastMessageTime) }}</span>
                </div>
                <p class="text-xs text-text-secondary truncate">{{ conv.lastMessage || '暂无消息' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ============================================================ -->
      <!-- 右侧：消息区域                                                  -->
      <!-- ============================================================ -->
      <div class="flex-1 flex flex-col min-w-0">
        <!-- 未选择会话 -->
        <div v-if="!selectedConvId" class="flex-1 flex flex-col items-center justify-center">
          <svg class="w-16 h-16 text-gray-200 mb-4" fill="none" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <p class="text-sm text-text-placeholder">选择左侧会话开始聊天</p>
        </div>

        <template v-else>
          <!-- 会话头部 -->
          <div class="flex items-center gap-3 px-5 py-3 border-b border-gray-50">
            <div class="w-8 h-8 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-xs font-bold text-brand">
              {{ selectedConv?.otherUserNickname?.charAt(0) || 'U' }}
            </div>
            <p class="text-sm font-semibold text-text-primary">{{ selectedConv?.otherUserNickname }}</p>
          </div>

          <!-- 消息流 -->
          <div ref="messageListRef" class="flex-1 overflow-y-auto px-5 py-4 space-y-3">
            <div v-if="messagesLoading" class="flex items-center justify-center py-8">
              <svg class="animate-spin w-5 h-5 text-brand" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
            </div>
            <template v-for="(msg, idx) in messages" :key="msg.id">
              <!-- 日期分隔 -->
              <div v-if="showDateSeparator(idx)" class="text-center py-2">
                <span class="text-[10px] text-text-placeholder bg-white px-3 py-0.5 rounded-full border border-gray-100">{{ formatFullDate(msg.createTime) }}</span>
              </div>
              <!-- 消息气泡 -->
              <div :class="['flex gap-2', msg.senderId === authStore.userId ? 'justify-end' : 'justify-start']">
                <!-- 对方消息：头像在左 -->
                <div v-if="msg.senderId !== authStore.userId" class="w-7 h-7 rounded-full bg-gradient-to-br from-gray-200 to-gray-300 flex items-center justify-center text-[10px] font-bold text-text-secondary flex-shrink-0">
                  {{ msg.senderNickname?.charAt(0) || 'U' }}
                </div>
                <div
                  :class="[
                    'max-w-[60%] px-3 py-2 rounded-2xl text-sm leading-relaxed break-words',
                    msg.senderId === authStore.userId
                      ? 'bg-brand text-white rounded-br-md'
                      : 'bg-gray-100 text-text-primary rounded-bl-md',
                  ]"
                >
                  <template v-if="msg.isRecalled">
                    <span class="text-text-placeholder italic">此消息已撤回</span>
                  </template>
                  <template v-else>
                    {{ msg.content }}
                  </template>
                </div>
                <!-- 自己消息：头像在右 -->
                <div v-if="msg.senderId === authStore.userId" class="w-7 h-7 rounded-full bg-gradient-to-br from-brand/30 to-brand-light/40 flex items-center justify-center text-[10px] font-bold text-brand flex-shrink-0">
                  {{ authStore.user?.nickname?.charAt(0) || '我' }}
                </div>
              </div>
            </template>
          </div>

          <!-- 输入框 -->
          <div class="border-t border-gray-50 px-4 py-3">
            <div class="flex items-end gap-3">
              <textarea
                v-model="newContent"
                class="flex-1 resize-none border border-gray-200 rounded-xl px-3 py-2 text-sm text-text-primary placeholder:text-text-placeholder focus:outline-none focus:border-brand/40 focus:ring-2 focus:ring-brand/10"
                rows="2"
                placeholder="输入消息..."
                :disabled="isSubmitting"
                @keydown="handleKeydown"
              />
              <button
                class="h-9 px-4 rounded-full bg-brand text-white text-sm font-medium hover:bg-brand-dark transition-colors flex-shrink-0 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                :disabled="isSubmitting || !newContent.trim()"
                @click="handleSend"
              >发送</button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
