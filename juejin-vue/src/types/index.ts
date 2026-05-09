// 登录请求（后端 LoginDTO 字段名为 email/phone，非 account）
export interface LoginDTO {
  email?: string
  phone?: string
  password: string
  loginType: 'email' | 'phone'
}

// 登录响应（字段名为后端 LoginVO 真实字段）
export interface LoginVO {
  userId: number
  accessToken: string
  refreshToken: string
  expiresIn: number
}

// 用户信息
export interface UserVO {
  id: number
  email: string
  phone: string
  nickname: string
  avatar: string
  bio: string
  level: number
  points: number
  status: number
  role: number
  createTime: string
}

// 掘力值
export interface PointsVO {
  points: number
  level: number
  levelName: string
}

// 关注状态
export interface FollowStatusVO {
  isFollowing: boolean
  isFollowedBy: boolean
}

// 关注用户
export interface FollowUserVO {
  id: number
  userId: number
  nickname: string
  avatar: string
  bio: string
  followTime: string
}

// 用户公开资料
export interface UserProfileVO {
  id: number
  nickname: string
  avatar: string
  bio: string
  backgroundImage: string
  level: number
  points: number
  followingCount: number
  followerCount: number
  articleCount: number
  likeCount: number
  totalViewCount: number
  title: string
  tags: TagBriefVO[]
  socialLinks: SocialLinkVO[]
  privacy: PrivacyVO
  badges: UserBadgeVO[]
}

export interface TagBriefVO {
  id: number
  name: string
}

export interface SocialLinkVO {
  linkType: string
  linkUrl: string
}

export interface PrivacyVO {
  showFavorites: boolean
  showFollowing: boolean
  showFollowers: boolean
  allowStrangerMessage: boolean
  messagePushEnabled: boolean
}

export interface UserBadgeVO {
  id: number
  badgeId: number
  name: string
  icon: string
  obtainTime: string
}

// 文章
export interface ArticleVO {
  id: number
  title: string
  summary: string
  coverImage: string
  content: string
  contentHtml: string
  categoryId: number
  categoryName: string
  authorId: number
  authorNickname: string
  authorAvatar: string
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  isTop: boolean
  isEssence: boolean
  isOriginal: boolean
  tags: TagBriefVO[]
  publishTime: string
  createTime: string
  liked: boolean
  favorited: boolean
}

export interface CategoryVO {
  id: number
  name: string
  description: string
  icon: string
  articleCount: number
}

// 徽章
export interface BadgeVO {
  id: number
  name: string
  description: string
  icon: string
  type: string
}

// 标签
export interface TagVO {
  id: number
  name: string
  description: string
  icon: string
  articleCount: number
  followCount: number
}

// 沸点
export interface PinVO {
  id: number
  userId: number
  content: string
  images: string[]
  linkUrl: string
  linkTitle: string
  topicId: number
  topicName: string
  likeCount: number
  commentCount: number
  userNickname: string
  userAvatar: string
  createTime: string
}

// 评论
export interface CommentVO {
  id: number
  userId: number
  targetId: number
  targetType: number
  content: string
  parentId: number | null
  rootId: number | null
  replyUserId: number | null
  likeCount: number
  replyCount: number
  isTop: boolean
  userNickname: string
  userAvatar: string
  replyUserNickname: string
  createTime: string
  replies: CommentVO[]
}

// 通知
export interface NotificationVO {
  id: number
  type: string
  title: string
  content: string
  targetId: number | null
  targetType: string
  senderId: number | null
  senderNickname: string
  senderAvatar: string
  isRead: boolean
  readTime: string | null
  createTime: string
}

// 签到
export interface SignVO {
  signed: boolean
  signDate: string
  continuousDays: number
  pointsEarned: number
  bonusDesc: string
  signedDates: string[]
  totalPoints: number
}

// 任务
export interface TaskVO {
  id: number
  name: string
  description: string
  type: string
  taskCode: string
  conditionValue: number
  pointsReward: number
  progress: number
  isCompleted: boolean
  isClaimed: boolean
}

// 沸点话题
export interface PinTopicVO {
  id: number
  name: string
  description: string
  coverImage: string
  pinCount: number
  followCount: number
  isHot: boolean
}

// Feed 流条目
export interface FeedVO {
  id: number
  type: 'article' | 'pin'
  title?: string
  content?: string
  summary?: string
  coverImage?: string
  authorId: number
  authorNickname: string
  authorAvatar: string
  likeCount: number
  commentCount: number
  viewCount?: number
  tags?: TagBriefVO[]
  createTime: string
}

// 收藏夹
export interface FavoriteFolderVO {
  id: number
  name: string
  description: string
  isPublic: boolean
  articleCount: number
}

// 收藏记录
export interface FavoriteRecordVO {
  id: number
  articleId: number
  title: string
  summary: string
  coverImage: string
  authorNickname: string
  favoriteTime: string
}

// 分页参数
export interface PageParam {
  page: number
  size: number
  sortBy?: string
}

// 统一响应
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}
