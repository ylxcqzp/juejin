# 项目完成计划

## 一、架构分析

### 1.1 现有架构评估

#### ✅ 已完成的合理设计
1. **微服务拆分合理**：5个核心服务（gateway、user、content、social、operation）符合业务边界
2. **技术栈选型正确**：Spring Boot 2.6.13 + Spring Cloud Alibaba 2021.0.5.0 + JDK 17
3. **分层架构规范**：entity、mapper、service、controller分层清晰
4. **公共模块设计**：common模块提取了基础实体、统一响应、异常处理等
5. **JWT认证实现**：网关统一认证，支持白名单配置
6. **简化RBAC方案**：使用role字段（0-普通用户 1-认证作者 2-管理员）

#### ⚠️ 需要改进的地方（全部已解决）

1. **~~缺少Service实现类~~** ✅ 已完成 - 所有服务均有完整Service实现
2. **~~缺少Mapper XML文件~~** ⭕ 可选 - MyBatis-Plus支持注解方式，复杂查询已用lambdaQuery实现
3. **~~实体类不完整~~** ✅ 已完成 - 所有30+实体已创建
4. **~~缺少OpenFeign调用~~** ✅ 已完成 - UserFeignClient + ContentFeignClient + FeignConfig
5. **~~缺少RabbitMQ配置~~** ⏸️ 后续 - MQ用于异步优化，当前同步Feign调用已满足需求
6. **~~缺少Swagger文档~~** ✅ 已完成 - Knife4j配置 + 所有Controller已注解

### 1.2 目录结构规范检查

根据用户规范要求：
- ✅ Controller层仅负责接收请求、参数校验、返回结果
- ✅ Service层处理核心业务逻辑
- ✅ Mapper层使用MyBatis-Plus接口
- ✅ Entity实体类与数据库表一一对应
- ✅ 使用Lombok简化代码
- ✅ 异常响应格式统一
- ✅ 简化RBAC权限控制（role字段方案）
- ✅ Swagger注解已添加到所有Controller

---

## 二、完成计划

### Phase 1: 完善基础服务 ✅ 已完成

**状态**：User服务完整实现，含所有实体、Mapper、Service、Controller（2026-05-03）

#### Task 1.1: 完善User服务实体和Mapper ✅
- [x] 创建UserOauth实体 ✅
- [x] 创建UserFollow实体 ✅
- [x] 创建UserSocialLink实体 ✅
- [x] 创建UserTagRelation实体 ✅
- [x] 创建Badge/UserBadge实体 ✅
- [x] 创建User实体 ✅
- [x] 创建所有Mapper接口 ✅（8个）

#### Task 1.2: 完善User服务业务逻辑 ✅
- [x] 创建UserServiceImpl ✅
- [x] 创建FollowServiceImpl ✅
- [x] 创建PrivacyServiceImpl ✅
- [x] 创建BadgeServiceImpl ✅

#### Task 1.3: 完善User服务Controller ✅
- [x] 创建UserController ✅（23个端点：注册/登录/资料/密码/标签/隐私/徽章/账号管理）
- [x] 创建AuthController ✅（刷新Token/重置密码）
- [x] 创建FollowController ✅（关注/取关/列表/移除粉丝/状态）
- [x] 添加Swagger注解 ✅

#### Task 1.4: User服务配置和工具 ✅
- [x] JwtUtils已实现 ✅
- [x] RedisKey常量类已创建 ✅
- [x] MyBatis-Plus分页配置 ✅
- [x] VO转换（BeanUtils）✅

### Phase 2: 创建Content服务 ✅ 已完成

**状态**：Content服务完整实现，含Article/Tag/Category/Draft全部CRUD（2026-05-03）

#### Task 2.1: Content服务实体和Mapper ✅
- [x] 创建Article实体 ✅
- [x] 创建ArticleVersion实体 ✅
- [x] 创建Tag实体 ✅
- [x] 创建Category实体 ✅
- [x] 创建Draft实体 ✅
- [x] 创建AuditRecord实体 ✅
- [x] 创建对应的Mapper接口 ✅（7个）

#### Task 2.2: Content服务业务层 ✅
- [x] 创建ArticleServiceImpl ✅（含审核流、版本管理、搜索、计数）
- [x] 创建TagServiceImpl ✅
- [x] 创建CategoryServiceImpl ✅
- [x] 创建DraftServiceImpl ✅

#### Task 2.3: Content服务控制层 ✅
- [x] 创建ArticleController ✅（13个端点：CRUD/审核/版本/搜索/回滚）
- [x] 创建TagController ✅（6个端点）
- [x] 创建CategoryController ✅（6个端点）
- [x] 创建DraftController ✅（6个端点）
- [x] 创建ContentInternalController ✅（Feign内部调用）

### Phase 3: 创建Social服务 ✅ 已完成

**状态**：所有社交功能已完整实现（2026-05-03）

#### Task 3.1: Social服务实体和Mapper ✅
- [x] 创建LikeRecord实体（带乐观锁）
- [x] 创建Comment实体
- [x] 创建Pin实体
- [x] 创建PinTopic实体
- [x] 创建FavoriteFolder实体
- [x] 创建FavoriteRecord实体
- [x] 创建对应的Mapper接口（7个）

#### Task 3.2: Social服务业务层 ✅
- [x] 创建LikeService（含Redis计数器）
- [x] 创建CommentService
- [x] 创建PinService
- [x] 创建FavoriteService
- [x] 创建FeedService
- [x] 创建NotificationService

#### Task 3.3: Social服务控制层 ✅
- [x] 创建LikeController
- [x] 创建CommentController
- [x] 创建PinController
- [x] 创建FavoriteController
- [x] 创建FeedController
- [x] 创建NotificationController

### Phase 4: 创建Operation服务 ✅ 已完成

**状态**：签到、任务功能已实现，已集成掘力值系统（2026-05-03）

#### Task 4.1-4.3: Operation服务
- [x] 创建SignRecord实体
- [x] 创建Task实体
- [x] 创建UserTask实体
- [x] 创建Badge实体（在user-service中）
- [x] 创建SignService + SignController
- [x] 创建TaskService + TaskController

### Phase 5: 服务间通信和基础设施 ✅ 已完成

#### Task 5.1: OpenFeign配置 ✅
- [x] 在Common模块创建Feign客户端接口
- [x] 创建UserFeignClient（getUserById, checkUserExists, addPoints）
- [x] 创建ContentFeignClient（文章计数变更）
- [x] 配置Feign请求拦截器（自动转发Authorization + X-User-Id）

#### Task 5.2: RabbitMQ配置 ⏸️ 后续
- 当前使用同步Feign调用，MQ用于后续异步优化

#### Task 5.3: Swagger文档 ✅
- [x] 创建Knife4j配置类（Knife4jConfig）
- [x] 创建Swagger自动配置（JuejinSwaggerAutoConfiguration）
- [x] 为所有Controller添加@Tag/@Operation/@Schema注解（62+文件）
- [x] Gateway白名单配置Swagger路径

#### Task 5.4: 分布式事务（Seata）⏸️ 后续
- 当前微服务规模无需分布式事务

### Phase 6: 前端Vue项目完善 ✅ 已完成

**状态**：前端13个页面全部实现，API接口层完整（2026-05-03）

#### Task 6.1: 项目结构调整 ✅
- [x] 创建 api/ 目录（12个资源模块 + index.ts 统一导出）
- [x] 创建 stores/ 目录（auth.ts, app.ts）
- [x] 创建 views/ 目录（13个页面）
- [x] 创建 components/ 目录（AppHeader, UserAvatar）

#### Task 6.2: 安装依赖和配置 ✅
- [x] Tailwind CSS 4（@tailwindcss/vite）
- [x] Pinia 3（状态管理）
- [x] Vue Router 4（路由 + 导航守卫）
- [x] Axios（HTTP + JWT拦截器）
- [x] marked + highlight.js（Markdown编辑器）
- [x] Vite 配置（API代理）

#### Task 6.3: 基础组件 ✅
- [x] AppHeader（导航栏 + 搜索 + 用户菜单）
- [x] UserAvatar（可复用头像组件）
- [x] 登录/注册页面（完整表单验证 + API对接）

#### Task 6.4: 页面开发 ✅（全部13个页面）
- [x] 首页（推荐/关注/热门三Tab + 瀑布流 + 无限滚动）
- [x] 文章详情页（目录导航 + 嵌套评论 + 点赞/收藏/关注）
- [x] 文章编辑器（Markdown编辑+预览 + 自动保存 + 草稿/发布）
- [x] 用户主页（资料卡 + 统计 + 文章/沸点/收藏Tab）
- [x] 沸点页（列表 + 发布 + 热门话题侧栏）
- [x] 标签页（网格/列表视图切换）
- [x] 分类页（分类信息 + 文章列表）
- [x] 搜索页（关键词搜索 + 结果高亮）
- [x] 通知中心（类型筛选 + 未读标记 + 全部已读）
- [x] 个人设置（资料/密码/隐私/社交链接）
- [x] 草稿箱（列表 + 继续编辑 + 删除）

### Phase 7: ~~测试和优化~~ ⏸️ 后续阶段

**说明**：测试和优化在核心功能完成后进行

---

## 三、优先级排序（更新）

### 高优先级（已全部完成）
1. ✅ 创建Social服务（点赞、评论、收藏、沸点、Feed、通知）
2. ✅ 配置OpenFeign服务间调用
3. ✅ 完善Swagger文档注解
4. ✅ 前端Vue项目完整搭建（13个页面 + 12个API模块）

### 中优先级（已完成）
1. ✅ 完善用户关注功能
2. ✅ 完善文章收藏功能
3. ✅ 文章审核流程
4. ✅ 前端完整功能开发（全部页面已实现）

### 低优先级（后续优化）
1. ⏸️ RabbitMQ消息队列配置
2. ⏸️ 分布式事务Seata
3. ⏸️ 单元测试与集成测试
4. ⏸️ 性能优化
5. ⏸️ 部署配置与CI/CD

---

## 四、简化方案说明

### 4.1 当前已实现（V1.0 完成版）
- ✅ 用户注册/登录（邮箱 + 手机号）
- ✅ JWT认证（Access + Refresh Token）
- ✅ 文章CRUD（创建、编辑、删除、列表、搜索、审核）
- ✅ 基础RBAC权限（role字段：0=普通用户 1=作者 2=管理员）
- ✅ 点赞功能（文章/评论/沸点，Redis + Lua + Feign同步）
- ✅ 评论功能（嵌套回复，最多2层）
- ✅ 收藏功能（多收藏夹管理 + 公开/私密）
- ✅ 沸点功能（发布、列表、话题、图片）
- ✅ Feed流（推荐/关注/热门）
- ✅ 关注/粉丝（双向关注状态、隐私设置）
- ✅ 消息通知（点赞/评论/关注/系统通知、未读计数）
- ✅ 签到/任务系统（每日签到、新手/日常任务、掘力值奖励）
- ✅ 标签/分类管理
- ✅ 草稿管理（保存、自动保存、发布）
- ✅ 前端完整页面（13个页面 + API层 + 状态管理）
- ✅ Swagger/Knife4j文档（所有Controller已注解）

### 4.2 后续增强（V2.0）
- ⏸️ 消息队列异步化（点赞/评论/通知使用RabbitMQ削峰）
- ⏸️ 全文搜索（Elasticsearch）
- ⏸️ 专栏/文集功能
- ⏸️ 文件上传服务
- ⏸️ 单元测试 + 集成测试
- ⏸️ CI/CD + Docker部署

---

## 五、开发建议

### 5.1 代码规范
- 严格遵循阿里巴巴Java开发手册
- 所有公共方法必须添加JavaDoc注释
- 使用统一的异常处理机制
- 接口返回统一使用Result包装

### 5.2 数据库规范
- 所有表必须有通用字段（create_time, update_time等）
- 高并发表必须添加version乐观锁字段
- 索引命名规范：idx_字段名
- 外键必须建立索引

### 5.3 接口规范
- RESTful API设计
- 统一响应格式：{code, message, data, timestamp}
- 分页参数：page, size, sort
- 认证Header：X-User-Id

### 5.4 安全规范
- 密码使用BCrypt加密 ✅
- JWT Token有效期30分钟 ✅
- 敏感接口需要认证 ✅
- 防止SQL注入（使用预编译）✅
