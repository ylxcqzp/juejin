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

#### ⚠️ 需要改进的地方

1. **~~缺少Service实现类~~** ✅ 已完成 - UserServiceImpl和ArticleServiceImpl已实现
2. **~~缺少Mapper XML文件~~** ⭕ 可选 - MyBatis-Plus支持注解方式，复杂查询才需要XML
3. **~~实体类不完整~~** ✅ 已完成 - User、Article、Category、Tag实体已创建
4. **缺少OpenFeign调用**：服务间通信需要添加Feign客户端
5. **缺少RabbitMQ配置**：消息队列尚未配置
6. **~~缺少Swagger文档~~** ✅ 基础配置已存在 - 需要为接口添加注解

### 1.2 目录结构规范检查

根据用户规范要求：
- ✅ Controller层仅负责接收请求、参数校验、返回结果
- ✅ Service层处理核心业务逻辑
- ✅ Mapper层使用MyBatis-Plus接口
- ✅ Entity实体类与数据库表一一对应
- ✅ 使用Lombok简化代码
- ✅ 异常响应格式统一
- ✅ 简化RBAC权限控制（role字段方案）

需要补充：
- ⚠️ ~~缺少BO（Business Object）层~~ - 简化方案中可省略，直接用DTO/VO
- ⚠️ ~~缺少MapStruct对象转换~~ - 当前使用BeanUtils，后续可优化
- ⚠️ 缺少Swagger注解 - 需要为Controller添加

---

## 二、完成计划

### Phase 1: 完善基础服务 ✅ 已完成核心功能

**状态**：User服务和Content服务核心功能已完成

#### Task 1.1: ~~完善User服务实体和Mapper~~ ✅ 已完成
- [x] ~~创建UserOauth实体~~ - 简化方案暂不实现
- [x] ~~创建UserFollow实体~~ - 简化方案暂不实现
- [x] ~~创建UserSocialLink实体~~ - 简化方案暂不实现
- [x] ~~创建UserTag实体~~ - 简化方案暂不实现
- [x] 创建User实体 ✅
- [x] 创建UserMapper接口 ✅

#### Task 1.2: ~~完善User服务业务逻辑~~ ✅ 已完成
- [x] 创建UserServiceImpl ✅
- [x] ~~创建FollowService~~ - 简化方案暂不实现
- [x] ~~创建UserOauthService~~ - 简化方案暂不实现

#### Task 1.3: ~~完善User服务Controller~~ ✅ 已完成
- [x] 创建UserController ✅
- [x] ~~创建FollowController~~ - 简化方案暂不实现
- [ ] 添加Swagger注解 ⏳ 待补充

#### Task 1.4: User服务配置和工具 ✅ 已完成
- [x] ~~创建JWT配置类~~ - JwtUtils已实现 ✅
- [x] ~~创建Redis Key常量类~~ - RedisKey常量类已创建 ✅
- [x] ~~添加MyBatis-Plus分页配置~~ - 已配置 ✅
- [x] ~~创建VO转换工具类~~ - 使用BeanUtils ✅

### Phase 2: ~~创建Content服务~~ ✅ 已完成核心功能

**状态**：Content服务核心功能已完成

#### Task 2.1: ~~Content服务实体和Mapper~~ ✅ 已完成
- [x] 创建Article实体 ✅
- [x] ~~创建ArticleVersion实体~~ - 简化方案暂不实现
- [x] 创建Tag实体 ✅
- [x] 创建Category实体 ✅
- [x] ~~创建Column实体~~ - 简化方案暂不实现
- [x] ~~创建Draft实体~~ - 简化方案暂不实现
- [x] 创建对应的Mapper接口 ✅

#### Task 2.2: ~~Content服务业务层~~ ✅ 已完成
- [x] 创建ArticleService接口 ✅
- [x] 创建ArticleServiceImpl ✅
- [x] ~~创建TagService~~ - 简化方案暂不实现
- [x] ~~创建CategoryService~~ - 简化方案暂不实现
- [x] ~~创建ColumnService~~ - 简化方案暂不实现

#### Task 2.3: ~~Content服务控制层~~ ✅ 已完成
- [x] 创建ArticleController ✅
- [x] ~~创建TagController~~ - 简化方案暂不实现
- [x] ~~创建CategoryController~~ - 简化方案暂不实现
- [x] ~~创建ColumnController~~ - 简化方案暂不实现

### Phase 3: 创建Social服务 ⏳ 待实现

#### Task 3.1: Social服务实体和Mapper
- [ ] 创建LikeRecord实体（带乐观锁）
- [ ] 创建Comment实体
- [ ] ~~创建Pin实体~~ - 简化方案暂不实现
- [ ] ~~创建PinTopic实体~~ - 简化方案暂不实现
- [ ] ~~创建FavoriteFolder实体~~ - 简化方案暂不实现
- [ ] ~~创建FavoriteRecord实体~~ - 简化方案暂不实现
- [ ] 创建对应的Mapper接口

#### Task 3.2: Social服务业务层
- [ ] 创建LikeService（含Redis计数器）
- [ ] 创建CommentService
- [ ] ~~创建PinService~~ - 简化方案暂不实现
- [ ] ~~创建FavoriteService~~ - 简化方案暂不实现

#### Task 3.3: Social服务控制层
- [ ] 创建LikeController
- [ ] 创建CommentController
- [ ] ~~创建PinController~~ - 简化方案暂不实现
- [ ] ~~创建FavoriteController~~ - 简化方案暂不实现

### Phase 4: ~~创建Operation服务~~ ⏸️ 低优先级

**说明**：签到、任务、徽章功能属于增强功能，简化方案中暂不实现

#### Task 4.1-4.3: Operation服务
- [ ] ~~创建SignRecord实体~~ - 暂不实现
- [ ] ~~创建Task实体~~ - 暂不实现
- [ ] ~~创建Badge实体~~ - 暂不实现
- [ ] ~~创建SignService~~ - 暂不实现
- [ ] ~~创建TaskService~~ - 暂不实现

### Phase 5: 服务间通信和基础设施 ⏳ 待实现

#### Task 5.1: OpenFeign配置 ⏳ 高优先级
- [ ] 在Common模块创建Feign客户端接口
- [ ] 创建UserFeignClient
- [ ] ~~创建ContentFeignClient~~ - 按需创建
- [ ] 配置Feign拦截器传递Token

#### Task 5.2: ~~RabbitMQ配置~~ ⏸️ 中优先级
- [ ] ~~创建RabbitMQ配置类~~ - 暂不实现
- [ ] ~~创建消息队列常量~~ - 暂不实现
- [ ] ~~创建生产者工具类~~ - 暂不实现
- [ ] ~~创建消费者示例~~ - 暂不实现

**说明**：当前阶段使用同步调用即可，MQ用于后续异步优化

#### Task 5.3: Swagger文档 ⏳ 高优先级
- [ ] 在Gateway配置Swagger聚合
- [ ] 在各服务添加SpringDoc依赖
- [ ] 创建Swagger配置类
- [ ] 为所有Controller添加Swagger注解

#### Task 5.4: ~~分布式事务（Seata）~~ ⏸️ 低优先级
- [ ] ~~添加Seata依赖~~ - 暂不实现
- [ ] ~~创建Seata配置~~ - 暂不实现

### Phase 6: 前端Vue项目完善 ⏳ 待实现

#### Task 6.1: Vue项目结构调整
- [ ] 按照技术文档调整目录结构
- [ ] 创建api/目录和接口文件
- [ ] 创建stores/目录和Pinia状态管理
- [ ] 创建views/目录结构
- [ ] 创建components/目录结构

#### Task 6.2: 安装依赖和配置
- [ ] 安装Tailwind CSS
- [ ] 安装Headless UI
- [ ] 安装Pinia
- [ ] 安装Vue Router
- [ ] 安装Axios
- [ ] 配置Vite

#### Task 6.3: 基础组件开发
- [ ] 创建Navbar组件
- [ ] 创建ArticleCard组件
- [ ] 创建Markdown编辑器组件
- [ ] 创建登录/注册页面

#### Task 6.4: 页面开发
- [ ] 首页（推荐流）
- [ ] 文章详情页
- [ ] 文章编辑页
- [ ] 用户个人主页
- [ ] 搜索页面

### Phase 7: ~~测试和优化~~ ⏸️ 后续阶段

**说明**：测试和优化在核心功能完成后进行

---

## 三、优先级排序（更新）

### 高优先级（下一步实现）
1. ⏳ 创建Social服务（点赞、评论）
2. ⏳ 配置OpenFeign服务间调用
3. ⏳ 完善Swagger文档注解
4. ⏳ 前端Vue项目基础搭建

### 中优先级（功能增强）
1. ⏸️ 配置RabbitMQ消息队列
2. ⏸️ 完善用户关注功能
3. ⏸️ 完善文章收藏功能
4. ⏸️ 前端完整功能开发

### 低优先级（后续优化）
1. ⏸️ 创建Operation服务（签到、任务）
2. ⏸️ 分布式事务Seata
3. ⏸️ 单元测试
4. ⏸️ 性能优化
5. ⏸️ 部署配置

---

## 四、简化方案说明

### 4.1 当前已实现（MVP版本）
- ✅ 用户注册/登录
- ✅ JWT认证
- ✅ 文章CRUD
- ✅ 基础RBAC权限（role字段）

### 4.2 下一步目标（V1.0版本）
- ⏳ 点赞功能
- ⏳ 评论功能
- ⏳ 前端基础页面
- ⏳ Swagger文档

### 4.3 后续增强（V2.0版本）
- ⏸️ 关注/粉丝
- ⏸️ 收藏功能
- ⏸️ 消息通知
- ⏸️ 签到/任务

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
