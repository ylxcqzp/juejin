// ============================================================
// Mock 数据 — 模拟掘金技术社区的真实数据
// 覆盖所有 API 端点，数据逼真、复杂、齐全
// ============================================================

import type {
  UserVO, UserProfileVO, ArticleVO, CommentVO, PinVO, PinTopicVO,
  FeedVO, NotificationVO, TagVO, CategoryVO, TagBriefVO,
  FollowUserVO, FollowStatusVO, BadgeVO,
  FavoriteFolderVO, FavoriteRecordVO, SignVO, TaskVO, PointsVO,
  LoginVO, PageResult,
} from '@/types'

// ---------- 用户 ----------
export const mockLoginVO: LoginVO = {
  userId: 1, accessToken: 'mock-jwt-token-xxx', refreshToken: 'mock-refresh-token-xxx', expiresIn: 1800,
}

export const mockCurrentUser: UserVO = {
  id: 1, email: 'zhangsan@juejin.com', phone: '138****8888', nickname: '张三前端',
  avatar: '', bio: '高级前端工程师 | Vue/React/Node.js | 掘金优秀作者 | 就职于某头部互联网公司',
  level: 5, points: 12860, status: 1, role: 1, createTime: '2024-03-15T10:00:00',
}

export const mockUsers: UserVO[] = [
  mockCurrentUser,
  { id: 2, email: 'lisi@juejin.com', phone: '', nickname: '李四全栈', avatar: '', bio: '全栈开发，专注Java/Spring Cloud', level: 4, points: 8900, status: 1, role: 1, createTime: '2024-02-20T08:30:00' },
  { id: 3, email: 'wangwu@juejin.com', phone: '', nickname: '王五AI', avatar: '', bio: 'AI研究员 | LLM/Agent/RAG', level: 6, points: 25600, status: 1, role: 2, createTime: '2023-08-10T14:00:00' },
  { id: 4, email: 'zhao@juejin.com', phone: '', nickname: '赵六后端', avatar: '', bio: '10年Java老兵，专注高并发', level: 4, points: 7200, status: 1, role: 1, createTime: '2024-05-01T16:00:00' },
  { id: 5, email: 'sun@juejin.com', phone: '', nickname: '孙七设计', avatar: '', bio: 'UI/UX设计师 | Figma重度用户', level: 2, points: 3200, status: 1, role: 0, createTime: '2024-08-15T09:00:00' },
]

// ---------- 标签 ----------
export const mockTags: TagVO[] = [
  { id: 1, name: 'Vue.js', description: '渐进式JavaScript框架', icon: '', articleCount: 12580, followCount: 96000 },
  { id: 2, name: 'React', description: '用于构建用户界面的JavaScript库', icon: '', articleCount: 15200, followCount: 110000 },
  { id: 3, name: 'Spring Boot', description: '简化Spring应用开发的框架', icon: '', articleCount: 8900, followCount: 68000 },
  { id: 4, name: 'TypeScript', description: 'JavaScript的超集，添加了类型系统', icon: '', articleCount: 6700, followCount: 52000 },
  { id: 5, name: 'Go', description: 'Google开发的静态强类型编程语言', icon: '', articleCount: 4500, followCount: 38000 },
  { id: 6, name: 'Rust', description: '内存安全的系统编程语言', icon: '', articleCount: 3200, followCount: 28000 },
  { id: 7, name: 'Docker', description: '容器化平台', icon: '', articleCount: 5600, followCount: 45000 },
  { id: 8, name: 'Kubernetes', description: '容器编排平台', icon: '', articleCount: 4100, followCount: 36000 },
  { id: 9, name: 'AI/LLM', description: '大语言模型与人工智能', icon: '', articleCount: 9800, followCount: 125000 },
  { id: 10, name: 'MySQL', description: '关系型数据库管理系统', icon: '', articleCount: 3400, followCount: 29000 },
]

export const mockTagBriefs: TagBriefVO[] = mockTags.slice(0, 10).map(t => ({ id: t.id, name: t.name }))

// ---------- 分类 ----------
// 首页左侧分类导航（仿掘金）
export const mockCategories: CategoryVO[] = [
  { id: 0, name: '综合', description: '', icon: 'icon-code', articleCount: 128000 },
  { id: 1, name: '前端', description: 'HTML/CSS/JS/Vue/React', icon: 'icon-code', articleCount: 45000 },
  { id: 2, name: '后端', description: 'Java/Go/Python/Node', icon: 'icon-server', articleCount: 38000 },
  { id: 3, name: 'AI', description: '人工智能/机器学习/深度学习', icon: 'icon-cpu', articleCount: 22000 },
  { id: 4, name: 'Android', description: 'Android开发', icon: 'icon-mobile', articleCount: 18000 },
  { id: 5, name: 'iOS', description: 'iOS/Swift开发', icon: 'icon-mobile', articleCount: 12000 },
  { id: 6, name: '人工智能', description: '大模型/CV/NLP', icon: 'icon-cpu', articleCount: 25000 },
  { id: 7, name: '开发工具', description: 'IDE/效率工具', icon: 'icon-tool', articleCount: 9000 },
  { id: 8, name: '代码人生', description: '程序员生活/职场', icon: 'icon-coffee', articleCount: 15000 },
  { id: 9, name: '阅读', description: '读书笔记/书评推荐', icon: 'icon-book', articleCount: 6000 },
]

// Markdown 文章原文（Vite raw import）
import m1 from './articles/1-vue3-composition-api.md?raw'
import m2 from './articles/2-springcloud-microservice.md?raw'
import m3 from './articles/3-lora-llm-finetune.md?raw'
import m4 from './articles/4-typescript5-new-features.md?raw'
import m5 from './articles/5-k8s-best-practices.md?raw'
import { marked } from '@/utils/marked-setup'

function htmlFromMD(md: string): string {
  return marked.parse(md) as string
}

// ---------- 文章 ----------
export const mockArticles: ArticleVO[] = [
  {
    id: 1, title: 'Vue 3 Composition API 实战：从入门到精通',
    summary: '本文详细讲解 Vue 3 Composition API 的核心概念、响应式原理、Composables 封装及 TypeScript 类型推断，包含大量代码示例和性能优化技巧。',
    content: m1, contentHtml: htmlFromMD(m1),
    coverImage: '', categoryId: 1, categoryName: '前端', authorId: 1, authorNickname: '张三前端', authorAvatar: '',
    status: 2, viewCount: 28600, likeCount: 3200, commentCount: 156, favoriteCount: 890,
    isTop: true, isEssence: true, isOriginal: true,
    tags: [{ id: 1, name: 'Vue.js' }, { id: 4, name: 'TypeScript' }],
    publishTime: '2025-04-28T10:30:00', createTime: '2025-04-27T22:00:00',
    liked: true, favorited: false,
  },
  {
    id: 2, title: 'Spring Cloud Alibaba 微服务实战：从零搭建企业级架构',
    summary: '基于 Spring Cloud Alibaba 全家桶，手把手搭建完整的微服务架构，涵盖 Nacos、Gateway、Feign、Sentinel 等核心组件的配置与最佳实践。',
    content: m2, contentHtml: htmlFromMD(m2),
    coverImage: '', categoryId: 5, categoryName: '架构', authorId: 4, authorNickname: '赵六后端', authorAvatar: '',
    status: 2, viewCount: 42500, likeCount: 5100, commentCount: 320, favoriteCount: 2100,
    isTop: false, isEssence: true, isOriginal: true,
    tags: [{ id: 3, name: 'Spring Boot' }],
    publishTime: '2025-04-20T08:00:00', createTime: '2025-04-19T18:00:00',
    liked: false, favorited: true,
  },
  {
    id: 3, title: 'LLM 微调实战：基于 LoRA 高效微调大语言模型',
    summary: '详解 LoRA 低秩适应技术原理，使用 HuggingFace PEFT 库对 LLaMA-2-7B 进行高效微调。对比全量微调/QLoRA/Prefix Tuning 的显存与性能，附完整 PyTorch 代码。',
    content: m3, contentHtml: htmlFromMD(m3),
    coverImage: '', categoryId: 6, categoryName: '人工智能', authorId: 3, authorNickname: '王五AI', authorAvatar: '',
    status: 2, viewCount: 56300, likeCount: 7800, commentCount: 430, favoriteCount: 3500,
    isTop: false, isEssence: true, isOriginal: true,
    tags: [{ id: 9, name: 'AI/LLM' }],
    publishTime: '2025-04-15T14:20:00', createTime: '2025-04-14T09:00:00',
    liked: false, favorited: false,
  },
  {
    id: 4, title: 'TypeScript 5.0 新特性全解析：装饰器、const 类型参数等',
    summary: 'TypeScript 5.0 带来了装饰器标准化、const 类型参数、枚举联合等重量级新特性。本文逐一解析并给出实战示例，帮助开发者快速上手。',
    content: m4, contentHtml: htmlFromMD(m4),
    coverImage: '', categoryId: 1, categoryName: '前端', authorId: 2, authorNickname: '李四全栈', authorAvatar: '',
    status: 2, viewCount: 18400, likeCount: 2100, commentCount: 98, favoriteCount: 650,
    isTop: false, isEssence: true, isOriginal: true,
    tags: [{ id: 4, name: 'TypeScript' }, { id: 2, name: 'React' }],
    publishTime: '2025-04-10T16:00:00', createTime: '2025-04-09T20:00:00',
    liked: true, favorited: true,
  },
  {
    id: 5, title: 'Kubernetes 生产环境最佳实践（2025 版）',
    summary: '总结 K8s 在生产环境中的资源配额、健康检查、滚动更新、HPA 自动伸缩、Pod 反亲和性等 10 条核心实践，每条附 YAML 配置示例。',
    content: m5, contentHtml: htmlFromMD(m5),
    coverImage: '', categoryId: 7, categoryName: '开发工具', authorId: 4, authorNickname: '赵六后端', authorAvatar: '',
    status: 2, viewCount: 32000, likeCount: 4500, commentCount: 210, favoriteCount: 1600,
    isTop: false, isEssence: true, isOriginal: true,
    tags: [{ id: 7, name: 'Docker' }, { id: 8, name: 'Kubernetes' }],
    publishTime: '2025-03-28T09:00:00', createTime: '2025-03-27T20:00:00',
    liked: true, favorited: false,
  },
  // 待审核文章（作者自己可见）
  {
    id: 101, title: '我的新文章：Vite 6 构建性能优化指南', summary: '探索 Vite 6 的最新构建优化功能，包括 Rolldown 集成、HMR 性能提升等。',
    content: '# Vite 6 构建性能优化\n\n文章正在审核中，审核通过后将公开可见。', contentHtml: '<h1>Vite 6 构建性能优化</h1><p>文章正在审核中，审核通过后将公开可见。</p>',
    coverImage: '', categoryId: 1, categoryName: '前端', authorId: 1, authorNickname: '张三前端', authorAvatar: '',
    status: 1, viewCount: 0, likeCount: 0, commentCount: 0, favoriteCount: 0,
    isTop: false, isEssence: false, isOriginal: true,
    tags: [{ id: 1, name: 'Vue.js' }],
    publishTime: '', createTime: '2025-05-02T10:00:00',
    liked: false, favorited: false,
  },
  {
    id: 102, title: '关于 Go 泛型的深入思考与实践', summary: 'Go 1.21 泛型使用一年后的经验总结。',
    content: '# Go 泛型深入思考\n\n已驳回，修改后重新提交。', contentHtml: '<h1>Go 泛型深入思考</h1><p>已驳回，修改后重新提交。</p>',
    coverImage: '', categoryId: 2, categoryName: '后端', authorId: 1, authorNickname: '张三前端', authorAvatar: '',
    status: 3, viewCount: 0, likeCount: 0, commentCount: 0, favoriteCount: 0,
    isTop: false, isEssence: false, isOriginal: true,
    tags: [{ id: 5, name: 'Go' }],
    publishTime: '', createTime: '2025-05-01T15:00:00',
    liked: false, favorited: false,
  },
]

// ---------- 评论 ----------
export const mockComments: CommentVO[] = [
  {
    id: 1, userId: 2, targetId: 1, targetType: 1,
    content: '写得非常好！Composition API确实比Options API更灵活，特别是逻辑复用这块。最近在用setup语法糖，代码简洁了很多。',
    parentId: null, rootId: null, replyUserId: null,
    likeCount: 128, replyCount: 3, isTop: true,
    userNickname: '李四全栈', userAvatar: '', replyUserNickname: '',
    createTime: '2025-04-29T08:30:00',
    replies: [
      {
        id: 11, userId: 1, targetId: 1, targetType: 1,
        content: '感谢认可！setup语法糖确实是最推荐的方式，配合TypeScript类型推断也很完美。',
        parentId: 1, rootId: 1, replyUserId: 2,
        likeCount: 45, replyCount: 0, isTop: false,
        userNickname: '张三前端', userAvatar: '', replyUserNickname: '李四全栈',
        createTime: '2025-04-29T09:00:00', replies: [],
      },
    ],
  },
  {
    id: 2, userId: 4, targetId: 1, targetType: 1,
    content: '请问setup中如何使用watchEffect来监听多个ref的变化？有没有好的实践分享？',
    parentId: null, rootId: null, replyUserId: null,
    likeCount: 56, replyCount: 1, isTop: false,
    userNickname: '赵六后端', userAvatar: '', replyUserNickname: '',
    createTime: '2025-04-29T10:15:00',
    replies: [],
  },
  {
    id: 3, userId: 5, targetId: 1, targetType: 1,
    content: '好文收藏！正好在从Vue 2迁移到Vue 3，这篇文章帮了大忙。',
    parentId: null, rootId: null, replyUserId: null,
    likeCount: 32, replyCount: 0, isTop: false,
    userNickname: '孙七设计', userAvatar: '', replyUserNickname: '',
    createTime: '2025-04-30T14:00:00', replies: [],
  },
  {
    id: 4, userId: 1, targetId: 3, targetType: 1,
    content: 'LoRA微调确实是目前性价比最高的方案，想问下你用的基座模型是哪个版本的LLaMA？',
    parentId: null, rootId: null, replyUserId: null,
    likeCount: 89, replyCount: 0, isTop: false,
    userNickname: '张三前端', userAvatar: '', replyUserNickname: '',
    createTime: '2025-04-16T10:00:00', replies: [],
  },
]

// ---------- 沸点 ----------
export const mockPins: PinVO[] = [
  {
    id: 1, userId: 3, content: '刚读完「Attention is All You Need」原论文，Transformer架构的精妙之处在于自注意力机制的并行计算能力。建议大家读原论文，比看解读强多了。', images: [], linkUrl: '', linkTitle: '', topicId: 1, topicName: 'AI讨论',
    likeCount: 890, commentCount: 45, userNickname: '王五AI', userAvatar: '', createTime: '2025-05-02T16:30:00',
  },
  {
    id: 2, userId: 1, content: 'Vue 3.5 的响应式系统又双叒优化了！这次主要是对深层嵌套对象的追踪性能提升近40%。Vue团队太强了。', images: [], linkUrl: '', linkTitle: '', topicId: 2, topicName: '前端趋势',
    likeCount: 650, commentCount: 32, userNickname: '张三前端', userAvatar: '', createTime: '2025-05-02T14:20:00',
  },
  {
    id: 3, userId: 2, content: '今天用Rust重构了一个Go微服务，内存占用从2GB降到200MB，CPU使用率也下降了60%。Rust的性能优势确实惊人，但开发效率确实比Go低不少，写起来需要更多时间。大家觉得值得吗？', images: [], linkUrl: '', linkTitle: '', topicId: 3, topicName: 'Rust vs Go',
    likeCount: 1200, commentCount: 180, userNickname: '李四全栈', userAvatar: '', createTime: '2025-05-02T10:00:00',
  },
  {
    id: 4, userId: 4, content: '分享一个K8s调试小技巧：kubectl debug命令可以在运行中的Pod中启动临时调试容器。比exec进去手动装工具方便多了！', images: [], linkUrl: 'https://kubernetes.io/docs/tasks/debug/debug-application/debug-running-pod/', linkTitle: 'Kubernetes官方文档 - Debug Running Pods', topicId: 4, topicName: 'DevOps实践',
    likeCount: 450, commentCount: 28, userNickname: '赵六后端', userAvatar: '', createTime: '2025-05-01T18:00:00',
  },
  {
    id: 5, userId: 5, content: '新设计的掘金App界面原型完成了，用了Figma的新功能Variables来管理设计令牌，效率提升了不少。有设计同行一起交流吗？', images: [], linkUrl: '', linkTitle: '', topicId: 5, topicName: '设计交流',
    likeCount: 320, commentCount: 15, userNickname: '孙七设计', userAvatar: '', createTime: '2025-05-01T12:00:00',
  },
]

export const mockPinTopics: PinTopicVO[] = [
  { id: 1, name: 'AI讨论', description: '人工智能相关话题讨论', coverImage: '', pinCount: 12500, followCount: 68000, isHot: true },
  { id: 2, name: '前端趋势', description: '前端技术趋势与前沿', coverImage: '', pinCount: 8900, followCount: 45000, isHot: true },
  { id: 3, name: 'Rust vs Go', description: '两种语言的对比与讨论', coverImage: '', pinCount: 3400, followCount: 22000, isHot: true },
  { id: 4, name: 'DevOps实践', description: '运维与开发实践交流', coverImage: '', pinCount: 5600, followCount: 31000, isHot: false },
  { id: 5, name: '设计交流', description: 'UI/UX设计师交流圈', coverImage: '', pinCount: 2100, followCount: 12000, isHot: false },
]

// ---------- Feed 流 ----------
export const mockFeeds: FeedVO[] = [
  { id: 1, type: 'article', title: 'Vue 3 Composition API 实战：从入门到精通', summary: '本文详细讲解Vue 3 Composition API的核心概念...', coverImage: '', authorId: 1, authorNickname: '张三前端', authorAvatar: '', likeCount: 3200, commentCount: 156, viewCount: 28600, tags: [{ id: 1, name: 'Vue.js' }], createTime: '2025-04-28T10:30:00' },
  { id: 3, type: 'article', title: 'LLM微调实战：基于LoRA高效微调大语言模型', summary: '详解LoRA低秩适应技术原理...', coverImage: '', authorId: 3, authorNickname: '王五AI', authorAvatar: '', likeCount: 7800, commentCount: 430, viewCount: 56300, tags: [{ id: 9, name: 'AI/LLM' }], createTime: '2025-04-15T14:20:00' },
  { id: 2, type: 'pin', content: '今天用Rust重构了一个Go微服务，内存占用从2GB降到200MB...', authorId: 2, authorNickname: '李四全栈', authorAvatar: '', likeCount: 1200, commentCount: 180, createTime: '2025-05-02T10:00:00' },
  { id: 4, type: 'article', title: 'TypeScript 5.0 新特性全解析', summary: 'TypeScript 5.0带来了诸多新特性...', coverImage: '', authorId: 2, authorNickname: '李四全栈', authorAvatar: '', likeCount: 2100, commentCount: 98, viewCount: 18400, tags: [{ id: 4, name: 'TypeScript' }], createTime: '2025-04-10T16:00:00' },
  { id: 1, type: 'pin', content: '刚读完「Attention is All You Need」原论文，Transformer架构的精妙之处...', authorId: 3, authorNickname: '王五AI', authorAvatar: '', likeCount: 890, commentCount: 45, createTime: '2025-05-02T16:30:00' },
  { id: 6, type: 'article', title: 'Kubernetes生产环境最佳实践（2025版）', summary: '总结K8s在生产环境中的20条最佳实践...', coverImage: '', authorId: 4, authorNickname: '赵六后端', authorAvatar: '', likeCount: 4500, commentCount: 210, viewCount: 32000, tags: [{ id: 7, name: 'Docker' }, { id: 8, name: 'Kubernetes' }], createTime: '2025-03-28T09:00:00' },
  { id: 2, type: 'pin', content: 'Vue 3.5 的响应式系统又双叒优化了...', authorId: 1, authorNickname: '张三前端', authorAvatar: '', likeCount: 650, commentCount: 32, createTime: '2025-05-02T14:20:00' },
  { id: 5, type: 'article', title: 'Rust异步编程深入：Tokio运行时原理与实战', summary: '深入理解Rust异步编程模型...', coverImage: '', authorId: 3, authorNickname: '王五AI', authorAvatar: '', likeCount: 1800, commentCount: 75, viewCount: 12200, tags: [{ id: 6, name: 'Rust' }], createTime: '2025-04-05T11:00:00' },
  { id: 4, type: 'pin', content: '分享一个K8s调试小技巧：kubectl debug命令...', authorId: 4, authorNickname: '赵六后端', authorAvatar: '', likeCount: 450, commentCount: 28, createTime: '2025-05-01T18:00:00' },
  { id: 5, type: 'pin', content: '新设计的掘金App界面原型完成了...', authorId: 5, authorNickname: '孙七设计', authorAvatar: '', likeCount: 320, commentCount: 15, createTime: '2025-05-01T12:00:00' },
]

// ---------- 通知 ----------
export const mockNotifications: NotificationVO[] = [
  { id: 1, type: 'like', title: '李四全栈 赞了你的文章', content: '赞了「Vue 3 Composition API 实战」', targetId: 1, targetType: 'article', senderId: 2, senderNickname: '李四全栈', senderAvatar: '', isRead: false, readTime: null, createTime: '2025-05-03T08:30:00' },
  { id: 2, type: 'comment', title: '赵六后端 评论了你的文章', content: '"请问setup中如何使用watchEffect..."', targetId: 1, targetType: 'article', senderId: 4, senderNickname: '赵六后端', senderAvatar: '', isRead: false, readTime: null, createTime: '2025-05-03T07:15:00' },
  { id: 3, type: 'follow', title: '王五AI 关注了你', content: '', targetId: null, targetType: '', senderId: 3, senderNickname: '王五AI', senderAvatar: '', isRead: true, readTime: '2025-05-02T10:00:00', createTime: '2025-05-02T09:00:00' },
  { id: 4, type: 'system', title: '你的文章已被推荐到首页', content: '「Vue 3 Composition API 实战」因内容优质被推荐到掘金首页，将获得更多曝光。', targetId: 1, targetType: 'article', senderId: null, senderNickname: '', senderAvatar: '', isRead: false, readTime: null, createTime: '2025-05-01T12:00:00' },
  { id: 5, type: 'like', title: '孙七设计 赞了你的沸点', content: '赞了「Vue 3.5 的响应式系统又双叒优化了」', targetId: 2, targetType: 'pin', senderId: 5, senderNickname: '孙七设计', senderAvatar: '', isRead: true, readTime: '2025-05-01T10:00:00', createTime: '2025-05-01T08:00:00' },
  { id: 6, type: 'system', title: '你的掘力值突破12000', content: '恭喜！你的掘力值已达到12860，解锁了Level 5等级标识。继续保持高质量创作！', targetId: null, targetType: '', senderId: null, senderNickname: '', senderAvatar: '', isRead: false, readTime: null, createTime: '2025-04-30T14:00:00' },
]

// ---------- 关注 ----------
export const mockFollowing: FollowUserVO[] = [
  { id: 1, userId: 2, nickname: '李四全栈', avatar: '', bio: '全栈开发', followTime: '2024-06-15' },
  { id: 2, userId: 3, nickname: '王五AI', avatar: '', bio: 'AI研究员', followTime: '2024-07-01' },
  { id: 3, userId: 4, nickname: '赵六后端', avatar: '', bio: '10年Java老兵', followTime: '2024-08-20' },
]
export const mockFollowers: FollowUserVO[] = [
  { id: 1, userId: 2, nickname: '李四全栈', avatar: '', bio: '全栈开发', followTime: '2024-09-01' },
  { id: 2, userId: 3, nickname: '王五AI', avatar: '', bio: 'AI研究员', followTime: '2024-10-10' },
  { id: 3, userId: 5, nickname: '孙七设计', avatar: '', bio: 'UI/UX设计师', followTime: '2024-11-05' },
]
export const mockFollowStatus: FollowStatusVO = { isFollowing: true, isFollowedBy: true }

// ---------- 用户资料 ----------
export const mockUserProfile: UserProfileVO = {
  id: 1, nickname: '张三前端', avatar: '', bio: '高级前端工程师 | Vue/React/Node.js | 掘金优秀作者',
  backgroundImage: '', level: 5, points: 12860,
  followingCount: 128, followerCount: 3560, articleCount: 48, likeCount: 12500,
  totalViewCount: 156000, title: '高级前端工程师',
  tags: [{ id: 1, name: 'Vue.js' }, { id: 2, name: 'React' }, { id: 4, name: 'TypeScript' }, { id: 7, name: 'Docker' }],
  socialLinks: [{ linkType: 'github', linkUrl: 'https://github.com/zhangsan' }, { linkType: 'blog', linkUrl: 'https://zhangsan.dev' }],
  privacy: { showFavorites: true, showFollowing: true, showFollowers: true, allowStrangerMessage: false, messagePushEnabled: true },
  badges: [{ id: 1, badgeId: 1, name: '掘金优秀作者', icon: '', obtainTime: '2025-01-15' }, { id: 2, badgeId: 3, name: '连续签到100天', icon: '', obtainTime: '2025-03-20' }],
}

// ---------- 徽章 ----------
export const mockBadges: BadgeVO[] = [
  { id: 1, name: '掘金优秀作者', description: '累计获得10000+点赞', icon: '', type: 'achievement' },
  { id: 2, name: '新人报道', description: '注册掘金账号', icon: '', type: 'newbie' },
  { id: 3, name: '连续签到100天', description: '连续签到100天', icon: '', type: 'daily' },
  { id: 4, name: '掘力值突破5000', description: '掘力值达到5000', icon: '', type: 'achievement' },
]

// ---------- 收藏 ----------
export const mockFavoriteFolders: FavoriteFolderVO[] = [
  { id: 1, name: '默认收藏夹', description: '', isPublic: false, articleCount: 15 },
  { id: 2, name: '前端精华', description: '优质前端文章合集', isPublic: true, articleCount: 8 },
  { id: 3, name: '后端架构', description: '微服务、分布式相关', isPublic: true, articleCount: 5 },
]
export const mockFavoriteRecords: FavoriteRecordVO[] = [
  { id: 1, articleId: 2, title: 'Spring Cloud Alibaba 微服务实战', summary: '基于Spring Cloud Alibaba全家桶...', coverImage: '', authorNickname: '赵六后端', favoriteTime: '2025-04-22T10:00:00' },
  { id: 2, articleId: 4, title: 'TypeScript 5.0 新特性全解析', summary: 'TypeScript 5.0带来了诸多新特性...', coverImage: '', authorNickname: '李四全栈', favoriteTime: '2025-04-12T15:00:00' },
]

// ---------- 掘力值 ----------
export const mockPoints: PointsVO = { points: 12860, level: 5, levelName: '掘金大神' }

// ---------- 签到 ----------
export const mockSign: SignVO = {
  signed: true, signDate: '2025-05-03', continuousDays: 23, pointsEarned: 50,
  bonusDesc: '连续签到7天额外奖励+20掘力值',
  signedDates: ['2025-05-01', '2025-05-02', '2025-05-03'],
  totalPoints: 12860,
}

// ---------- 任务 ----------
export const mockTasks: TaskVO[] = [
  { id: 1, name: '每日签到', description: '完成每日签到', type: 'daily', taskCode: 'daily_sign', conditionValue: 1, pointsReward: 10, progress: 1, isCompleted: true, isClaimed: true },
  { id: 2, name: '发布一篇文章', description: '发布一篇原创技术文章', type: 'daily', taskCode: 'publish_article', conditionValue: 1, pointsReward: 50, progress: 1, isCompleted: true, isClaimed: false },
  { id: 3, name: '点赞5篇文章', description: '为5篇优秀文章点赞', type: 'daily', taskCode: 'like_5_articles', conditionValue: 5, pointsReward: 20, progress: 3, isCompleted: false, isClaimed: false },
  { id: 4, name: '完善个人资料', description: '设置头像、昵称和个人简介', type: 'newbie', taskCode: 'complete_profile', conditionValue: 1, pointsReward: 100, progress: 1, isCompleted: true, isClaimed: true },
  { id: 5, name: '获得100个点赞', description: '累计获得100个内容点赞', type: 'newbie', taskCode: 'get_100_likes', conditionValue: 100, pointsReward: 200, progress: 85, isCompleted: false, isClaimed: false },
]

// ========================================================================
// 给部分文章添加封面图
mockArticles.forEach((a, i) => {
  if (i % 3 !== 2) a.coverImage = `https://picsum.photos/seed/juejin${a.id}/320/200`
})

// 文章排行（右侧栏）
export const mockArticleRanking = mockArticles.filter(a => a.status === 2).sort((a, b) => b.viewCount - a.viewCount).slice(0, 5)
export const mockAuthorRanking = [
  { id: 1, nickname: '张三前端', avatar: '', level: 5, articleCount: 48 },
  { id: 3, nickname: '王五AI', avatar: '', level: 6, articleCount: 72 },
  { id: 4, nickname: '赵六后端', avatar: '', level: 4, articleCount: 35 },
  { id: 2, nickname: '李四全栈', avatar: '', level: 4, articleCount: 56 },
  { id: 5, nickname: '孙七设计', avatar: '', level: 2, articleCount: 12 },
]

// 辅助：创建分页结果
// ========================================================================

export function wrapPage<T>(list: T[], page = 1, size = 10): PageResult<T> {
  const start = (page - 1) * size
  return {
    list: list.slice(start, start + size),
    total: list.length,
    page,
    size,
  }
}
