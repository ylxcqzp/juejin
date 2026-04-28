# 项目完成计划

## 一、架构分析

### 1.1 现有架构评估

#### ✅ 已完成的合理设计
1. **微服务拆分合理**：5个核心服务（gateway、user、content、social、operation）符合业务边界
2. **技术栈选型正确**：Spring Boot 2.6.13 + Spring Cloud Alibaba 2021.0.5.0 + JDK 17
3. **分层架构规范**：entity、mapper、service、controller分层清晰
4. **公共模块设计**：common模块提取了基础实体、统一响应、异常处理等

#### ⚠️ 需要改进的地方

1. **缺少Service实现类**：UserService只有接口，需要完善实现类
2. **缺少Mapper XML文件**：需要创建对应的XML映射文件
3. **实体类不完整**：只创建了User实体，其他服务的实体需要补充
4. **缺少OpenFeign调用**：服务间通信需要添加Feign客户端
5. **缺少RabbitMQ配置**：消息队列尚未配置
6. **缺少Swagger文档**：API文档需要集成SpringDoc

### 1.2 目录结构规范检查

根据用户规范要求：
- ✅ Controller层仅负责接收请求、参数校验、返回结果
- ✅ Service层处理核心业务逻辑
- ✅ Mapper层使用MyBatis-Plus接口
- ✅ Entity实体类与数据库表一一对应
- ✅ 使用Lombok简化代码
- ✅ 异常响应格式统一

需要补充：
- ⚠️ 缺少BO（Business Object）层
- ⚠️ 缺少MapStruct对象转换
- ⚠️ 缺少Swagger注解

---

## 二、完成计划

### Phase 1: 完善User服务（基础服务模板）

**目标**：将user-service打造成完整的服务模板，其他服务参照此模式

#### Task 1.1: 完善User服务实体和Mapper
- [ ] 创建UserOauth实体
- [ ] 创建UserFollow实体
- [ ] 创建UserSocialLink实体
- [ ] 创建UserTag实体
- [ ] 创建对应的Mapper接口
- [ ] 创建Mapper XML文件

#### Task 1.2: 完善User服务业务逻辑
- [ ] 完善UserServiceImpl（补充缺失的方法）
- [ ] 创建FollowService（关注/取消关注）
- [ ] 创建FollowServiceImpl
- [ ] 创建UserOauthService（第三方登录）

#### Task 1.3: 完善User服务Controller
- [ ] 完善UserController（补充关注/粉丝列表接口）
- [ ] 创建FollowController
- [ ] 添加Swagger注解
- [ ] 添加参数校验注解

#### Task 1.4: User服务配置和工具
- [ ] 创建JWT配置类
- [ ] 创建Redis Key常量类
- [ ] 添加MyBatis-Plus分页配置
- [ ] 创建VO转换工具类

### Phase 2: 创建Content服务

#### Task 2.1: Content服务实体和Mapper
- [ ] 创建Article实体
- [ ] 创建ArticleVersion实体
- [ ] 创建Tag实体
- [ ] 创建Category实体
- [ ] 创建Column实体
- [ ] 创建Draft实体
- [ ] 创建对应的Mapper接口和XML

#### Task 2.2: Content服务业务层
- [ ] 创建ArticleService接口
- [ ] 创建ArticleServiceImpl
- [ ] 创建TagService
- [ ] 创建CategoryService
- [ ] 创建ColumnService

#### Task 2.3: Content服务控制层
- [ ] 创建ArticleController
- [ ] 创建TagController
- [ ] 创建CategoryController
- [ ] 创建ColumnController

### Phase 3: 创建Social服务

#### Task 3.1: Social服务实体和Mapper
- [ ] 创建LikeRecord实体（带乐观锁）
- [ ] 创建Comment实体
- [ ] 创建Pin实体
- [ ] 创建PinTopic实体
- [ ] 创建FavoriteFolder实体
- [ ] 创建FavoriteRecord实体
- [ ] 创建对应的Mapper接口和XML

#### Task 3.2: Social服务业务层
- [ ] 创建LikeService（含Redis Lua脚本）
- [ ] 创建CommentService
- [ ] 创建PinService
- [ ] 创建FavoriteService

#### Task 3.3: Social服务控制层
- [ ] 创建LikeController
- [ ] 创建CommentController
- [ ] 创建PinController
- [ ] 创建FavoriteController

### Phase 4: 创建Operation服务

#### Task 4.1: Operation服务实体和Mapper
- [ ] 创建SignRecord实体
- [ ] 创建Task实体
- [ ] 创建UserTask实体
- [ ] 创建Badge实体
- [ ] 创建UserBadge实体
- [ ] 创建对应的Mapper接口和XML

#### Task 4.2: Operation服务业务层
- [ ] 创建SignService（签到逻辑）
- [ ] 创建TaskService
- [ ] 创建BadgeService

#### Task 4.3: Operation服务控制层
- [ ] 创建SignController
- [ ] 创建TaskController
- [ ] 创建BadgeController

### Phase 5: 服务间通信和基础设施

#### Task 5.1: OpenFeign配置
- [ ] 在Common模块创建Feign客户端接口
- [ ] 创建UserFeignClient
- [ ] 创建ContentFeignClient
- [ ] 配置Feign拦截器传递Token

#### Task 5.2: RabbitMQ配置
- [ ] 创建RabbitMQ配置类
- [ ] 创建消息队列常量
- [ ] 创建生产者工具类
- [ ] 创建消费者示例

#### Task 5.3: Swagger文档
- [ ] 在Gateway配置Swagger聚合
- [ ] 在各服务添加SpringDoc依赖
- [ ] 创建Swagger配置类
- [ ] 为所有Controller添加Swagger注解

#### Task 5.4: 分布式事务（Seata）
- [ ] 添加Seata依赖
- [ ] 创建Seata配置
- [ ] 在需要的服务启用分布式事务

### Phase 6: 前端Vue项目完善

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

### Phase 7: 测试和优化

#### Task 7.1: 单元测试
- [ ] 为Service层编写单元测试
- [ ] 为Mapper层编写测试
- [ ] 配置H2内存数据库测试

#### Task 7.2: 集成测试
- [ ] 测试服务间调用
- [ ] 测试消息队列
- [ ] 测试缓存

#### Task 7.3: 性能优化
- [ ] 添加Redis缓存注解
- [ ] 优化SQL查询
- [ ] 配置连接池参数

#### Task 7.4: 部署配置
- [ ] 创建Dockerfile
- [ ] 创建docker-compose.yml
- [ ] 创建Kubernetes部署文件
- [ ] 配置Nginx反向代理

---

## 三、优先级排序

### 高优先级（必须完成）
1. 完善User服务（作为模板）
2. 创建Content服务核心功能
3. 创建Social服务核心功能
4. 配置Swagger文档
5. 前端基础页面

### 中优先级（重要功能）
1. 创建Operation服务
2. 配置OpenFeign
3. 配置RabbitMQ
4. 前端完整功能

### 低优先级（优化增强）
1. 分布式事务Seata
2. 单元测试
3. 性能优化
4. 部署配置

---

## 四、开发建议

### 4.1 代码规范
- 严格遵循阿里巴巴Java开发手册
- 所有方法必须添加JavaDoc注释
- 使用统一的异常处理机制
- 接口返回统一使用Result包装

### 4.2 数据库规范
- 所有表必须有通用字段（create_time, update_time等）
- 高并发表必须添加version乐观锁字段
- 索引命名规范：idx_字段名
- 外键必须建立索引

### 4.3 接口规范
- RESTful API设计
- 统一响应格式：{code, message, data, timestamp}
- 分页参数：page, size, sort
- 认证Header：X-User-Id

### 4.4 安全规范
- 密码使用BCrypt加密
- JWT Token有效期30分钟
- 敏感接口需要认证
- 防止SQL注入（使用预编译）
