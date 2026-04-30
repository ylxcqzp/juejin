# 掘金后端 API 接口文档

## 通用约定

### 统一响应格式

所有接口返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1714400000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | `Integer` | 状态码。200 成功，其他为错误码 |
| `message` | `String` | 提示信息 |
| `data` | `T` | 业务数据，错误时为 `null` |
| `timestamp` | `Long` | 响应时间戳（毫秒） |

### 认证方式

除白名单接口外，所有接口需在请求头携带 JWT Token：

```
Authorization: Bearer <accessToken>
```

**白名单接口（无需认证）：**
- `POST /api/v1/users/register` — 邮箱注册
- `POST /api/v1/users/login` — 登录
- `GET /api/v1/tags/**` — 标签查询
- `GET /api/v1/categories/**` — 分类查询
- `GET /api/v1/feed/**` — Feed 流
- `GET /api/v1/search/**` — 搜索
- `GET /api/v1/pins/**` — 沸点

### 分页格式

分页接口统一使用 `PageResult<T>` 结构：

```json
{
  "list": [],
  "total": 100,
  "page": 1,
  "size": 10
}
```

### 错误码表

| 码 | 说明 |
|----|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 405 | 请求方法不支持 |
| 500 | 服务繁忙，请稍后重试 |
| 1001 | 邮箱已存在 |
| 1002 | 手机号已存在 |
| 1003 | 账号或密码错误 |
| 1004 | 账号已被禁用 |
| 1007 | 用户不存在 |
| 1009 | 账号已锁定，请15分钟后重试 |
| 1010 | 验证码错误 |
| 2001 | 文章不存在 |
| 2003 | 无权限编辑此文 |
| 2004 | 标签不存在 |
| 2006 | 分类不存在 |
| 2008 | 草稿不存在 |
| 3003 | 评论不存在 |
| 3006 | 收藏夹不存在 |
| 3010 | 无删除权限 |
| 4001 | 今日已签到 |
| 4002 | 任务不存在 |

---

## 1. 用户服务（user-service）

**路径前缀**: `/api/v1/users`、`/api/v1/auth`

### 1.1 邮箱注册

```
POST /api/v1/users/register
```

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `email` | `String` | ✅ | 邮箱地址 |
| `password` | `String` | ✅ | 密码，最少6位 |
| `nickname` | `String` | ✅ | 昵称，最少2个字符 |

**响应 `Result<UserVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.id` | `Long` | 用户ID |
| `data.email` | `String` | 邮箱 |
| `data.nickname` | `String` | 昵称 |
| `data.avatar` | `String` | 头像URL |
| `data.level` | `Integer` | 用户等级（1-6） |
| `data.points` | `Integer` | 掘力值 |
| `data.createTime` | `String` | 注册时间 |

### 1.2 手机号注册

```
POST /api/v1/users/register/phone
```

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `phone` | `String` | ✅ | 手机号 |
| `code` | `String` | ✅ | 短信验证码 |
| `password` | `String` | ✅ | 密码 |
| `nickname` | `String` | ✅ | 昵称 |

**响应**: 同邮箱注册 `Result<UserVO>`

### 1.3 登录

```
POST /api/v1/users/login
```

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `email` | `String` | 条件 | 邮箱（loginType=email 时必填） |
| `phone` | `String` | 条件 | 手机号（loginType=phone 时必填） |
| `password` | `String` | ✅ | 密码 |
| `loginType` | `String` | — | 登录方式：`email`（默认）或 `phone` |

**响应 `Result<LoginVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.userId` | `Long` | 用户ID |
| `data.accessToken` | `String` | JWT 访问令牌（30分钟有效） |
| `data.refreshToken` | `String` | JWT 刷新令牌（7天有效） |
| `data.expiresIn` | `Long` | 令牌过期时间（秒） |

### 1.4 发送验证码

```
POST /api/v1/users/verify-code
```

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `account` | `String` | ✅ | 邮箱或手机号 |
| `type` | `String` | ✅ | `email` 或 `phone` |

### 1.5 刷新 Token

```
POST /api/v1/auth/refresh
```
> **认证**: `Authorization: Bearer <refreshToken>`

**响应**: 同登录 `Result<LoginVO>`

### 1.6 重置密码（发送验证码）

```
POST /api/v1/auth/password/reset
```

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `account` | `String` | ✅ | 邮箱或手机号 |
| `type` | `String` | ✅ | `email` 或 `phone` |

### 1.7 重置密码（确认）

```
POST /api/v1/auth/password/reset/confirm
```

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `account` | `String` | ✅ | 邮箱或手机号 |
| `code` | `String` | ✅ | 验证码 |
| `newPassword` | `String` | ✅ | 新密码 |

### 1.8 获取当前用户信息

```
GET /api/v1/users/current
```
> **认证**: ✅

**请求头**: `X-User-Id: <userId>`

**响应**: `Result<UserVO>`

### 1.9 获取用户信息

```
GET /api/v1/users/{id}
```

### 1.10 检查用户是否存在

```
GET /api/v1/users/{id}/exists
```
> **内部调用**

**响应**: `Result<Boolean>`

### 1.11 获取用户公开资料

```
GET /api/v1/users/{id}/profile
```

**响应 `Result<UserProfileVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.nickname` | `String` | 昵称 |
| `data.avatar` | `String` | 头像URL |
| `data.bio` | `String` | 个人简介 |
| `data.level` | `Integer` | 等级 |
| `data.points` | `Integer` | 掘力值 |
| `data.followingCount` | `Integer` | 关注数 |
| `data.followerCount` | `Integer` | 粉丝数 |
| `data.articleCount` | `Integer` | 文章数 |
| `data.likeCount` | `Integer` | 获赞数 |
| `data.tags` | `TagBriefVO[]` | 技能标签列表 |

### 1.12 获取我的个人资料

```
GET /api/v1/users/profile
```
> **认证**: ✅

**响应**: `Result<UserProfileVO>`（含隐私设置、社交链接、徽章）

### 1.13 更新个人资料

```
PUT /api/v1/users/profile
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `nickname` | `String` | 昵称 |
| `avatar` | `String` | 头像URL |
| `bio` | `String` | 个人简介 |
| `backgroundImage` | `String` | 主页背景图 |

### 1.14 修改密码

```
PUT /api/v1/users/password
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `oldPassword` | `String` | ✅ | 原密码 |
| `newPassword` | `String` | ✅ | 新密码 |

### 1.15 更新技能标签

```
PUT /api/v1/users/tags
```
> **认证**: ✅

**请求体**: `List<Long>` — 标签ID数组（最多5个）

### 1.16 更新社交链接

```
PUT /api/v1/users/social-links
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `links` | `SocialLinkItem[]` | 链接列表 |
| `links[].linkType` | `String` | 类型：github/blog/weibo 等 |
| `links[].linkUrl` | `String` | 链接URL |

### 1.17 获取掘力值与等级

```
GET /api/v1/users/points
```
> **认证**: ✅

**响应 `Result<PointsVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.points` | `Integer` | 掘力值 |
| `data.level` | `Integer` | 等级 |
| `data.levelName` | `String` | 等级称号 |

### 1.18 增加掘力值（内部调用）

```
POST /api/v1/users/{id}/points/add?points=10
```

### 1.19 获取用户徽章

```
GET /api/v1/users/badges
```
> **认证**: ✅

**响应 `Result<List<UserBadgeVO>>`**

### 1.20 获取全部徽章列表

```
GET /api/v1/users/badges/all
```

**响应 `Result<List<BadgeVO>>`**

### 1.21 绑定手机号

```
POST /api/v1/users/bind/phone
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `account` | `String` | ✅ | 手机号 |
| `code` | `String` | ✅ | 验证码 |

### 1.22 绑定邮箱

```
POST /api/v1/users/bind/email
```
> **认证**: ✅

**请求体**: 同绑定手机号

### 1.23 解绑账号

```
DELETE /api/v1/users/unbind/{type}
```
> **认证**: ✅
> `type`: `phone` 或 `email`

### 1.24 申请注销账号

```
POST /api/v1/users/account/cancel
```
> **认证**: ✅
> 进入30天冷静期，期间可撤销

### 1.25 撤销注销申请

```
POST /api/v1/users/account/cancel/revoke
```
> **认证**: ✅

---

### 1.26 关注用户

```
POST /api/v1/users/{id}/follow
```
> **认证**: ✅

### 1.27 取消关注

```
DELETE /api/v1/users/{id}/follow
```
> **认证**: ✅

### 1.28 获取关注列表

```
GET /api/v1/users/{id}/following?page=1&size=20
```

**响应**: `Result<PageResult<FollowUserVO>>`

### 1.29 获取粉丝列表

```
GET /api/v1/users/{id}/followers?page=1&size=20
```

**响应**: `Result<PageResult<FollowUserVO>>`

### 1.30 移除粉丝

```
DELETE /api/v1/users/followers/{id}
```
> **认证**: ✅

### 1.31 检查关注状态

```
GET /api/v1/users/{id}/follow/status
```
> **认证**: ✅

**响应 `Result<FollowStatusVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.isFollowing` | `Boolean` | 我是否关注了对方 |
| `data.isFollowedBy` | `Boolean` | 对方是否关注了我 |

---

### 1.32 隐私设置

#### 获取隐私设置

```
GET /api/v1/users/privacy
```
> **认证**: ✅

#### 更新隐私设置

```
PUT /api/v1/users/privacy
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `showFavorites` | `Boolean` | 公开收藏夹 |
| `showFollowing` | `Boolean` | 公开关注列表 |
| `showFollowers` | `Boolean` | 公开粉丝列表 |
| `allowStrangerMessage` | `Boolean` | 允许陌生人私信 |
| `messagePushEnabled` | `Boolean` | 消息推送 |

---

## 2. 内容服务（content-service）

**路径前缀**: `/api/v1/articles`、`/api/v1/tags`、`/api/v1/categories`、`/api/v1/drafts`

### 2.1 文章管理

#### 创建文章

```
POST /api/v1/articles
```
> **认证**: ✅

**请求体 `ArticleCreateDTO`**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `title` | `String` | ✅ | 标题，最多100字 |
| `content` | `String` | ✅ | Markdown 内容 |
| `contentHtml` | `String` | — | HTML 渲染内容 |
| `summary` | `String` | — | 摘要，最多200字 |
| `coverImage` | `String` | — | 封面图URL |
| `categoryId` | `Long` | — | 分类ID |
| `tagIds` | `List<Long>` | — | 标签ID列表，最多5个 |
| `isOriginal` | `Boolean` | — | 是否原创 |
| `scheduledTime` | `String` | — | 定时发布时间 |

**响应**: `Result<ArticleVO>`

#### 编辑文章

```
PUT /api/v1/articles/{id}
```
> **认证**: ✅（仅作者本人）

**请求体 `ArticleUpdateDTO`**: 同创建，`title` 和 `content` 必填

#### 获取文章详情

```
GET /api/v1/articles/{id}
```

**响应 `Result<ArticleVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.id` | `Long` | 文章ID |
| `data.title` | `String` | 标题 |
| `data.summary` | `String` | 摘要 |
| `data.content` | `String` | Markdown 正文 |
| `data.contentHtml` | `String` | HTML 正文 |
| `data.coverImage` | `String` | 封面图 |
| `data.authorId` | `Long` | 作者ID |
| `data.authorNickname` | `String` | 作者昵称 |
| `data.authorAvatar` | `String` | 作者头像 |
| `data.categoryId` | `Long` | 分类ID |
| `data.categoryName` | `String` | 分类名 |
| `data.tags` | `TagBriefVO[]` | 标签列表 |
| `data.viewCount` | `Integer` | 阅读量 |
| `data.likeCount` | `Integer` | 点赞数 |
| `data.commentCount` | `Integer` | 评论数 |
| `data.favoriteCount` | `Integer` | 收藏数 |
| `data.isOriginal` | `Boolean` | 是否原创 |
| `data.isTop` | `Boolean` | 是否置顶 |
| `data.liked` | `Boolean` | 当前用户是否已点赞 |
| `data.favorited` | `Boolean` | 当前用户是否已收藏 |
| `data.publishTime` | `String` | 发布时间 |
| `data.createTime` | `String` | 创建时间 |

#### 删除文章

```
DELETE /api/v1/articles/{id}
```
> **认证**: ✅（仅作者本人）

#### 文章列表

```
GET /api/v1/articles?page=1&size=10&categoryId=1&sortBy=latest
```

**参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `page` | `Integer` | — | 页码，默认1 |
| `size` | `Integer` | — | 每页条数，默认10 |
| `categoryId` | `Long` | — | 分类筛选 |
| `sortBy` | `String` | — | `latest` 最新 / `hot` 热门 |

**响应**: `Result<PageResult<ArticleVO>>`

#### 用户文章列表

```
GET /api/v1/articles/user/{userId}?page=1&size=10
```

#### 热门文章

```
GET /api/v1/articles/hot
```

#### 搜索文章

```
GET /api/v1/articles/search?keyword=vue&page=1&size=10
```

#### 提交审核

```
POST /api/v1/articles/{id}/review
```
> **认证**: ✅

#### 审核通过（管理员）

```
POST /api/v1/articles/{id}/approve
```
> **认证**: ✅

#### 审核驳回（管理员）

```
POST /api/v1/articles/{id}/reject
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `reason` | `String` | 驳回原因 |

#### 获取文章历史版本

```
GET /api/v1/articles/{id}/versions
```

**响应**: `Result<List<ArticleVersionVO>>`

#### 回滚到历史版本

```
POST /api/v1/articles/{id}/versions/{versionId}/rollback
```
> **认证**: ✅

---

### 2.2 标签管理

#### 获取全部标签

```
GET /api/v1/tags/all
```

#### 标签列表

```
GET /api/v1/tags?page=1&size=20
```

#### 标签详情

```
GET /api/v1/tags/{id}
```

#### 创建标签

```
POST /api/v1/tags
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `name` | `String` | ✅ | 标签名 |
| `description` | `String` | — | 描述 |
| `icon` | `String` | — | 图标 |

#### 更新标签

```
PUT /api/v1/tags/{id}
```
> **认证**: ✅

#### 删除标签

```
DELETE /api/v1/tags/{id}
```
> **认证**: ✅

---

### 2.3 分类管理

#### 获取全部分类

```
GET /api/v1/categories/all
```

#### 分类列表

```
GET /api/v1/categories?page=1&size=20
```

#### 分类详情

```
GET /api/v1/categories/{id}
```

#### 创建分类

```
POST /api/v1/categories
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `name` | `String` | ✅ | 分类名 |
| `description` | `String` | — | 描述 |

#### 更新分类

```
PUT /api/v1/categories/{id}
```
> **认证**: ✅

#### 删除分类

```
DELETE /api/v1/categories/{id}
```
> **认证**: ✅

---

### 2.4 草稿管理

#### 草稿列表

```
GET /api/v1/drafts?page=1&size=10
```
> **认证**: ✅

#### 草稿详情

```
GET /api/v1/drafts/{draftId}
```
> **认证**: ✅

#### 保存草稿

```
POST /api/v1/drafts
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 标题 |
| `content` | `String` | Markdown 内容 |
| `coverImage` | `String` | 封面图 |
| `categoryId` | `Long` | 分类ID |
| `tagIds` | `List<Long>` | 标签ID |

#### 自动保存草稿

```
PUT /api/v1/drafts/{draftId}/auto-save
```
> **认证**: ✅

#### 删除草稿

```
DELETE /api/v1/drafts/{draftId}
```
> **认证**: ✅

#### 从草稿发布

```
POST /api/v1/drafts/{draftId}/publish
```
> **认证**: ✅

---

### 2.5 内部接口（跨服务调用）

**路径前缀**: `/internal/articles`
> 不对外暴露

```
POST /internal/articles/{articleId}/like/increment
POST /internal/articles/{articleId}/like/decrement
POST /internal/articles/{articleId}/comment/increment
POST /internal/articles/{articleId}/comment/decrement
POST /internal/articles/{articleId}/favorite/increment
POST /internal/articles/{articleId}/favorite/decrement
```

---

## 3. 社交服务（social-service）

**路径前缀**: `/api/v1/likes`、`/api/v1/comments`、`/api/v1/favorites`、`/api/v1/pins`、`/api/v1/feed`、`/api/v1/notifications`

### 3.1 点赞

#### 切换点赞

```
POST /api/v1/likes
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `targetId` | `Long` | ✅ | 目标ID |
| `targetType` | `Integer` | ✅ | 1=文章 2=评论 3=沸点 |

**逻辑**: 未点赞则点赞，已点赞则取消

#### 获取点赞列表

```
GET /api/v1/likes?targetId=1&targetType=1&page=1&size=20
```

#### 检查点赞状态

```
GET /api/v1/likes/status?targetId=1&targetType=1
```
> **认证**: ✅

**响应**: `Result<Boolean>` — data 为 `true` 已点赞 / `false` 未点赞

---

### 3.2 评论

#### 发表评论

```
POST /api/v1/comments
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `targetId` | `Long` | ✅ | 目标ID |
| `targetType` | `Integer` | ✅ | 1=文章 2=沸点 |
| `content` | `String` | ✅ | 评论内容 |
| `parentId` | `Long` | — | 父评论ID（回复时传入） |
| `rootId` | `Long` | — | 根评论ID |
| `replyUserId` | `Long` | — | 被回复用户ID |

#### 删除评论

```
DELETE /api/v1/comments/{commentId}
```
> **认证**: ✅（仅评论发布者）

#### 置顶/取消置顶评论

```
PUT /api/v1/comments/{commentId}/top
```
> **认证**: ✅

#### 评论列表

```
GET /api/v1/comments?targetId=1&targetType=1&page=1&size=20&sortBy=latest
```

**参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `targetId` | `Long` | 目标ID |
| `targetType` | `Integer` | 目标类型 |
| `page` | `Integer` | 页码 |
| `size` | `Integer` | 每页条数 |
| `sortBy` | `String` | `latest` 最新 / `hot` 热门 |

**响应 `Result<PageResult<CommentVO>>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list[].id` | `Long` | 评论ID |
| `data.list[].userId` | `Long` | 评论者ID |
| `data.list[].content` | `String` | 评论内容 |
| `data.list[].userNickname` | `String` | 评论者昵称 |
| `data.list[].userAvatar` | `String` | 评论者头像 |
| `data.list[].likeCount` | `Integer` | 点赞数 |
| `data.list[].replyCount` | `Integer` | 回复数 |
| `data.list[].isTop` | `Boolean` | 是否置顶 |
| `data.list[].createTime` | `String` | 发布时间 |
| `data.list[].replies` | `CommentVO[]` | 子回复列表（最多1层） |

---

### 3.3 收藏

#### 获取收藏夹列表

```
GET /api/v1/favorites/folders
```
> **认证**: ✅

#### 创建收藏夹

```
POST /api/v1/favorites/folders
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `name` | `String` | ✅ | 收藏夹名称 |
| `description` | `String` | — | 描述 |
| `isPublic` | `Boolean` | — | 是否公开 |

#### 更新收藏夹

```
PUT /api/v1/favorites/folders/{folderId}
```
> **认证**: ✅

#### 删除收藏夹

```
DELETE /api/v1/favorites/folders/{folderId}
```
> **认证**: ✅

#### 收藏文章

```
POST /api/v1/favorites
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `articleId` | `Long` | ✅ | 文章ID |
| `folderId` | `Long` | — | 收藏夹ID（默认收藏夹） |

#### 取消收藏

```
DELETE /api/v1/favorites/{articleId}
```
> **认证**: ✅

#### 获取收藏夹文章列表

```
GET /api/v1/favorites/folders/{folderId}/articles?page=1&size=20
```

#### 检查收藏状态

```
GET /api/v1/favorites/status?articleId=1
```
> **认证**: ✅

---

### 3.4 沸点

#### 发布沸点

```
POST /api/v1/pins
```
> **认证**: ✅

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `content` | `String` | ✅ | 内容，最多1000字 |
| `images` | `String` | — | 图片URL，逗号分隔 |
| `linkUrl` | `String` | — | 链接URL |
| `topicId` | `Long` | — | 话题ID |

#### 沸点列表

```
GET /api/v1/pins?page=1&size=20&sortBy=latest
```

#### 关注用户的沸点

```
GET /api/v1/pins/following?page=1&size=20
```
> **认证**: ✅

#### 话题下的沸点

```
GET /api/v1/pins/topic/{topicId}?page=1&size=20
```

#### 热门话题

```
GET /api/v1/pins/topics/hot?limit=10
```

**响应**: `Result<List<PinTopicVO>>`

---

### 3.5 Feed 流

#### 关注流

```
GET /api/v1/feed/following?page=1&size=20
```
> **认证**: ✅

#### 推荐流

```
GET /api/v1/feed/recommend?page=1&size=20
```

#### 热门流

```
GET /api/v1/feed/hot?page=1&size=20
```

**响应**: `Result<PageResult<FeedVO>>`

---

### 3.6 通知

#### 通知列表

```
GET /api/v1/notifications?page=1&size=20&type=like
```
> **认证**: ✅

**参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `type` | `String` | 筛选类型：`like`/`comment`/`follow`/`system` |

**响应 `Result<PageResult<NotificationVO>>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list[].id` | `Long` | 通知ID |
| `data.list[].type` | `String` | 类型 |
| `data.list[].title` | `String` | 标题 |
| `data.list[].content` | `String` | 内容 |
| `data.list[].senderId` | `Long` | 触发者用户ID |
| `data.list[].senderNickname` | `String` | 触发者昵称 |
| `data.list[].isRead` | `Boolean` | 是否已读 |
| `data.list[].createTime` | `String` | 时间 |

#### 未读数量

```
GET /api/v1/notifications/unread-count
```
> **认证**: ✅

**响应**: `Result<Integer>`

#### 全部标记已读

```
PUT /api/v1/notifications/read-all
```
> **认证**: ✅

---

## 4. 运营服务（operation-service）

**路径前缀**: `/api/v1/signs`、`/api/v1/tasks`

### 4.1 签到

#### 签到

```
POST /api/v1/signs
```
> **认证**: ✅

**响应 `Result<SignVO>`**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `data.signed` | `Boolean` | 是否签到成功 |
| `data.signDate` | `String` | 签到日期 |
| `data.continuousDays` | `Integer` | 连续签到天数 |
| `data.pointsEarned` | `Integer` | 本次获得掘力值 |
| `data.bonusDesc` | `String` | 额外奖励描述 |
| `data.signedDates` | `String[]` | 当月已签到日期 |
| `data.totalPoints` | `Integer` | 当前总掘力值 |

#### 签到状态

```
GET /api/v1/signs/status
```
> **认证**: ✅

---

### 4.2 任务

#### 任务列表

```
GET /api/v1/tasks
```
> **认证**: ✅

**响应**: `Result<List<TaskVO>>`

#### 按类型获取任务

```
GET /api/v1/tasks/type/{type}?userId=1
```
> `type`: `daily` 日常任务 / `newbie` 新手任务

#### 领取任务奖励

```
POST /api/v1/tasks/{taskId}/claim
```
> **认证**: ✅

**请求头**: `X-User-Id: <userId>`

**响应**: `Result<TaskVO>`（含更新后进度和领取状态）
