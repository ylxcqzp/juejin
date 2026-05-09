# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**语言规则**: 全程使用中文回复，代码注释也必须使用中文。

**文档同步规则（硬性）**: 每次需求变更、新增技术组件、业务逻辑变更，必须同步更新以下文档：
1. `CLAUDE.md` — 项目架构/技术栈/新增模块/关键模式变更
2. `create_tables.sql` — 所有数据库表结构变更（DDL + 初始化数据）
3. 前端 `types/index.ts` — 接口类型变更
4. 前端 `api/` — 新增/修改 API 模块
5. 前端 `mock/data.ts` + `mock/handler.ts` — Mock 数据格式与真实接口保持一致

**UI 设计**: 涉及 UI 布局变更时，应调用 `web-design` skill 生成设计稿。

## Project Overview

A tech community platform (similar to juejin.im/稀土掘金) with a Spring Cloud Alibaba microservices backend and Vue 3 frontend. Features include article publishing, social interaction (likes/comments/favorites/pins), personalized feed, search, notifications, user growth system, and content moderation.

## Commands

### Backend (Maven, JDK 17)

```bash
# Build all modules
cd juejin-backend && mvn clean compile -DskipTests

# Build a single module with dependencies
mvn clean compile -pl juejin-user-service -am -DskipTests

# Run a specific service (from module directory)
mvn spring-boot:run -pl juejin-gateway
mvn spring-boot:run -pl juejin-user-service
mvn spring-boot:run -pl juejin-content-service
mvn spring-boot:run -pl juejin-social-service
mvn spring-boot:run -pl juejin-operation-service

# Run tests
mvn test
mvn test -pl juejin-user-service

# Check dependency tree
mvn dependency:tree -pl juejin-user-service
```

### Frontend (Vue 3 + Vite)

```bash
cd juejin-vue
npm run dev        # Start dev server
npm run build      # Type-check and build for production
npm run preview    # Preview production build
```

## Architecture

```
juejin-backend/
├── pom.xml                          # Parent POM, dependency versions, module list
├── juejin-framework/                # Shared infrastructure (6 sub-modules)
│   ├── juejin-common/               # Result, BaseEntity, ErrorCode, BusinessException, GlobalExceptionHandler, RedisKey, Feign clients
│   ├── juejin-starter-mybatis/      # MyBatis-Plus auto-config (pagination, optimistic-lock, BlockAttack, MetaObjectHandler)
│   ├── juejin-starter-redis/        # Redis/Redisson auto-config, RedisUtils (Jackson serializer)
│   ├── juejin-starter-mq/           # RabbitMQ auto-config, MqTemplate
│   ├── juejin-starter-security/     # JWT auto-config (JwtUtils reads jwt.* from config, NOT hardcoded)
│   └── juejin-starter-swagger/      # Knife4j auto-config
├── juejin-gateway/                  # Spring Cloud Gateway (port 8080) — routing, AuthFilter, CORS, Sentinel
├── juejin-user-service/             # User registration/login/profile/follow/badges (port 8081)
├── juejin-content-service/          # Articles, tags, categories (port 8082)
├── juejin-social-service/           # Likes, comments, favorites, pins, feed, notifications (port 8083)
└── juejin-operation-service/        # Sign-in, tasks (port 8084)
```

### Frontend (Vue 3 + TypeScript + Tailwind CSS 4)

```
juejin-vue/src/
├── api/                              # API 接口层（12个资源模块）
│   ├── request.ts                    # Axios实例 + JWT拦截器
│   ├── index.ts                      # 统一导出
│   ├── articles.ts / users.ts        # 内容/用户
│   ├── comments.ts / likes.ts        # 评论/点赞
│   ├── pins.ts / feed.ts             # 沸点/Feed流
│   ├── notifications.ts / drafts.ts  # 通知/草稿
│   ├── tags.ts / categories.ts       # 标签/分类
│   └── favorites.ts / signs.ts / tasks.ts  # 收藏/签到/任务
├── stores/                           # Pinia状态管理
│   ├── auth.ts                       # 登录/登出/Token刷新/用户缓存
│   └── app.ts                        # UI状态（侧边栏/菜单）
├── router/
│   └── index.ts                      # 13条路由 + 导航守卫
├── types/
│   └── index.ts                      # 25+ TypeScript接口
├── views/                            # 13个页面组件
│   ├── HomeView.vue                  # 首页（推荐/关注/热门三Tab + 瀑布流）
│   ├── ArticleDetailView.vue         # 文章详情（目录导航 + 嵌套评论）
│   ├── EditorView.vue                # Markdown编辑器（编辑+预览 + 自动保存）
│   ├── UserProfileView.vue           # 用户主页（资料卡 + 文章/沸点/收藏Tab）
│   ├── LoginView.vue / RegisterView.vue  # 登录/注册
│   ├── PinListView.vue               # 沸点列表 + 发布
│   ├── TagListView.vue               # 标签网格/列表
│   ├── CategoryView.vue              # 分类详情 + 文章列表
│   ├── SearchResultView.vue          # 搜索 + 关键词高亮
│   ├── NotificationView.vue          # 通知中心（类型筛选 + 未读标记）
│   ├── SettingsView.vue              # 设置（资料/密码/隐私/社交链接）
│   └── DraftListView.vue             # 草稿箱（继续编辑 + 删除）
├── components/
│   ├── AppHeader.vue                 # 全局导航栏
│   └── UserAvatar.vue                # 可复用头像组件
├── layouts/
│   ├── DefaultLayout.vue             # 默认布局（Header + Main）
│   └── AuthLayout.vue                # 登录/注册布局
└── style.css                         # Tailwind + 掘金品牌色定义
```

**API代理**: Vite代理 `/api/v1` → `localhost:8080`（Gateway）

**表单提交规范**: 所有表单提交和触发API调用的按钮都必须使用 `useSubmitLock` 进行节流控制，防止重复提交。请求进行中的按钮必须显示 loading 状态（`isSubmitting` ref）并禁用。

```ts
// composables/useSubmitLock.ts
import { useSubmitLock } from '@/composables/useSubmitLock'
const { isSubmitting, withLock } = useSubmitLock()
async function handleSubmit() {
  await withLock(async () => {
    await someApiCall()
  })
}
// 模板: :disabled="isSubmitting"
```

### Starter dependency model

**Configuration beans live in starters, NOT in common.** After the architecture refactoring:

- `juejin-common`: pure shared classes (Result, BaseEntity, ErrorCode, constants, Feign interface definitions) — NO @Configuration beans
- `juejin-starter-*`: each provides a specific @Configuration bean via `spring.factories` auto-configuration
- **Service modules must explicitly declare which starters they need** in their pom.xml

Example service dependency set:
```xml
<!-- user-service: needs DB + Redis + JWT + Swagger -->
<juejin-common />
<juejin-starter-mybatis />
<juejin-starter-redis />
<juejin-starter-security />
<juejin-starter-swagger />
```

### Service routing (gateway application.yml)

| Route | Target Service | Notes |
|-------|---------------|-------|
| `/api/v1/users/**`, `/api/v1/auth/**` | `juejin-user-service` | |
| `/api/v1/articles/**`, `/api/v1/tags/**`, `/api/v1/categories/**` | `juejin-content-service` | Article write ops require JWT |
| `/api/v1/likes/**`, `/api/v1/comments/**`, `/api/v1/favorites/**`, `/api/v1/pins/**`, `/api/v1/feed/**` | `juejin-social-service` | |
| `/api/v1/signs/**`, `/api/v1/tasks/**`, `/api/v1/uploads/**`, `/api/v1/search/**` | `juejin-operation-service` | |

### Auth flow

1. Login → JWT generated via `JwtUtils.generateToken()` (in `juejin-starter-security`)
2. Request → Gateway `AuthFilter` extracts Bearer token, validates with jjwt, injects `X-User-Id` header
3. Downstream service reads `@RequestHeader("X-User-Id") Long userId`
4. Whitelist configured in `auth.whitelist` (gateway application.yml) — register, login, GET tags/categories, Swagger docs

**Important:** Article `POST/PUT/DELETE` endpoints are NOT in the whitelist — they require valid JWT tokens.

## Key Patterns

### Unified response — `Result<T>` (`juejin-common`)
```java
Result.success(data)              // 200
Result.error(ErrorCode.XXX)       // Use ErrorCode enum
Result.error(code, "message")     // Custom error
```

### Error codes
`ErrorCode` enum ranges: 1000-1999 user, 2000-2999 content, 3000-3999 social, 4000-4999 operation, 5000-5999 file.

### Entity conventions
- Extend `BaseEntity` → provides `id` (auto-increment), `createTime`, `creator`, `updateTime`, `updater`, `deleted` (@TableLogic)
- MyBatis-Plus `id-type: auto`, `logic-delete-field: deleted` (0=normal, 1=deleted)
- Auto-fill handled by `CustomMetaObjectHandler` in `juejin-starter-mybatis`

### Service layer pattern
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {
    // Type-safe queries: lambdaQuery().eq().page()
    // Entity <-> VO: BeanUtils.copyProperties()
    // Cache: RedisKey.XXX + id, with TTL
    // Pagination: use PageResult.of(Ipage) or PageResult.of(list, total, page, size)
}
```

### PageResult — factory methods only
`PageResult<T>` has no public constructor. Use static factory methods:
```java
PageResult.of(iPage)                              // From MyBatis-Plus IPage
PageResult.of(List<T> list, Long total, Integer page, Integer size)
PageResult.of(List<T> list, Long cursor)           // Cursor-based pagination
```

### Redis key conventions
All Redis keys must use `RedisKey` constants (in `juejin-common`). Format: `prefix:id`.
```java
RedisKey.USER_INFO + userId       // "user:123"
RedisKey.ARTICLE_DETAIL + id      // "article:456"
```

### JWT utilities
Located at `com.juejin.starter.security.utils.JwtUtils` (static methods):
```java
JwtUtils.generateToken(userId)         // Access token (30min default)
JwtUtils.generateRefreshToken(userId)   // Refresh token (7d default)
JwtUtils.parseToken(token)             // Returns Claims
JwtUtils.validateToken(token)          // Returns boolean
```
Configured via `jwt.secret`, `jwt.expiration`, `jwt.refresh-expiration` in application.yml.

### Cross-service communication
- Feign interfaces: `com.juejin.common.feign` (e.g., `UserFeignClient`)
- Feign request interceptor: `FeignConfig` (auto-forwards Authorization + X-User-Id headers)

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| Spring Boot | 2.6.13 |
| Spring Cloud | 2021.0.5 |
| Spring Cloud Alibaba | 2021.0.5.0 |
| MySQL | 8.0 (端口 3307, 账号 root/root) |
| Nacos Server | 2.4.3 (端口 8848, namespace: dev) |
| Redis | 7.2.5 (端口 6379) |
| RabbitMQ | 3.9.13 |
| Sentinel | 1.8.6 |
| MyBatis-Plus | 3.5.3.1 |
| Knife4j | 4.1.0 |
| Druid | 1.2.16 |
| Redisson | 3.19.0 |
| Hutool | 5.8.11 |
| jjwt | 0.11.5 |
| fastjson2 | 2.0.20 |

## Naming conventions
- **DTO**: `*DTO` suffix, request body, `@Schema` annotations for Swagger
- **VO**: `*VO` suffix, response objects
- **Mapper**: `*Mapper` interface extends MyBatis-Plus `BaseMapper<T>`
- **Service**: `*Service` interface extends `IService<T>`, impl extends `ServiceImpl<M, T>`
- Packages: `*.controller`, `*.mapper`, `*.entity`, `*.service`, `*.service.impl`, `*.dto`, `*.vo`
- Nacos namespace: `dev` (unified across all services)

## Known caveats
- Gateway Sentinel requires `spring-cloud-alibaba-sentinel-gateway` dependency (not just `spring-cloud-starter-alibaba-sentinel`)
- `BaseEntity` fields `creator` and `updater` are annotated for auto-fill but the handler does not populate them — they remain null
- `ArticleServiceImpl.convertToVO` calls `categoryMapper.selectById` per-article (N+1 query), fine for detail pages but batch list endpoints may need optimization
- social-service and operation-service are fully implemented (May 2026) — 6 controllers + 2 controllers respectively
- Frontend EditorView chunk is large (~960KB) due to highlight.js — consider dynamic import if load time becomes an issue
