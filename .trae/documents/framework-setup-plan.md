# 项目框架搭建计划

## 一、架构调整说明

### 1.1 目录结构调整

根据新的架构要求，项目结构调整为：

```
juejin-backend/
├── pom.xml                                    # 父POM
├── juejin-framework/                          # 框架层（新增）
│   ├── juejin-common/                         # 公共核心模块（原位置移入）
│   ├── juejin-starter-mq/                     # MQ Starter（新增）
│   ├── juejin-starter-redis/                  # Redis Starter（新增）
│   ├── juejin-starter-mybatis/                # MyBatis增强Starter（新增）
│   ├── juejin-starter-security/               # Security Starter（新增）
│   └── juejin-starter-swagger/                # Swagger/Knife4j Starter（新增）
├── juejin-gateway/                            # 网关服务
├── juejin-user-service/                       # 用户服务
├── juejin-content-service/                    # 内容服务
├── juejin-social-service/                     # 社交服务
└── juejin-operation-service/                  # 运营服务
```

### 1.2 技术栈更新

| 组件 | 原方案 | 新方案 | 说明 |
|------|--------|--------|------|
| 工具类 | 部分自定义 | Hutool | 全面使用Hutool工具类 |
| 安全框架 | 自定义JWT | Spring Security + JWT | 完整的认证授权框架 |
| 接口文档 | SpringDoc | Knife4j + Swagger | 更友好的API文档 |
| 框架组织 | 平级模块 | juejin-framework目录 | 更好的模块管理 |

---

## 二、Phase 1: 创建juejin-framework框架层

### Task 1.1: 调整目录结构
- [ ] 创建juejin-framework目录
- [ ] 将juejin-common移动到juejin-framework下
- [ ] 更新父POM的modules配置
- [ ] 更新juejin-common的parent路径

### Task 1.2: 创建juejin-starter-mybatis
- [ ] 创建模块pom.xml
- [ ] 引入MyBatis-Plus、Druid依赖
- [ ] 创建MyBatisPlusConfig配置类
- [ ] 创建分页插件配置
- [ ] 创建自动配置类（spring.factories）
- [ ] 创建MetaObjectHandler自动填充

### Task 1.3: 创建juejin-starter-redis
- [ ] 创建模块pom.xml
- [ ] 引入Redisson、Redis依赖
- [ ] 创建RedisConfig配置类
- [ ] 创建RedisTemplate配置
- [ ] 创建RedissonClient配置
- [ ] 创建Redis工具类封装
- [ ] 创建缓存注解封装
- [ ] 创建自动配置类

### Task 1.4: 创建juejin-starter-mq
- [ ] 创建模块pom.xml
- [ ] 引入RabbitMQ依赖
- [ ] 创建RabbitMQConfig配置类
- [ ] 创建消息队列常量定义
- [ ] 创建生产者模板类
- [ ] 创建消费者基础类
- [ ] 创建自动配置类

### Task 1.5: 创建juejin-starter-security
- [ ] 创建模块pom.xml
- [ ] 引入Spring Security、JWT依赖
- [ ] 创建SecurityConfig配置类
- [ ] 创建JwtTokenProvider
- [ ] 创建JwtAuthenticationFilter
- [ ] 创建UserDetailsService接口
- [ ] 创建SecurityUtils工具类
- [ ] 创建自动配置类

### Task 1.6: 创建juejin-starter-swagger
- [ ] 创建模块pom.xml
- [ ] 引入Knife4j、Swagger依赖
- [ ] 创建Knife4jConfig配置类
- [ ] 创建Swagger属性配置
- [ ] 创建自动配置类

### Task 1.7: 完善juejin-common
- [ ] 引入Hutool依赖
- [ ] 移除自定义工具类，改用Hutool
- [ ] 创建基础实体类BaseEntity
- [ ] 创建统一响应结果Result
- [ ] 创建错误码枚举ErrorCode
- [ ] 创建业务异常BusinessException
- [ ] 创建全局异常处理器
- [ ] 创建分页参数PageParam
- [ ] 创建分页结果PageResult
- [ ] 创建通用常量类

---

## 三、Phase 2: 更新父POM和依赖管理

### Task 2.1: 更新根pom.xml
- [ ] 添加Hutool版本管理
- [ ] 添加Spring Security版本管理
- [ ] 添加Knife4j版本管理
- [ ] 更新modules列表
- [ ] 添加framework模块依赖管理

### Task 2.2: 更新服务模块pom.xml
- [ ] 移除重复依赖
- [ ] 添加framework starter依赖
- [ ] 统一依赖版本

---

## 四、Phase 3: 更新Gateway服务

### Task 3.1: 更新Gateway依赖
- [ ] 添加juejin-starter-security依赖（用于JWT解析）
- [ ] 移除重复的JWT依赖

### Task 3.2: 更新Gateway配置
- [ ] 更新application.yml
- [ ] 配置Knife4j文档聚合

### Task 3.3: 更新AuthFilter
- [ ] 使用Security模块的JWT工具
- [ ] 优化过滤器逻辑

---

## 五、Phase 4: 更新User服务（作为模板）

### Task 4.1: 更新User服务依赖
- [ ] 添加juejin-starter-mybatis依赖
- [ ] 添加juejin-starter-redis依赖
- [ ] 添加juejin-starter-security依赖
- [ ] 添加juejin-starter-swagger依赖
- [ ] 添加juejin-common依赖
- [ ] 移除重复依赖

### Task 4.2: 更新User服务配置
- [ ] 更新application.yml
- [ ] 配置Security
- [ ] 配置Swagger/Knife4j

### Task 4.3: 使用Hutool重构代码
- [ ] 使用Hutool的BeanUtil替换BeanUtils
- [ ] 使用Hutool的DigestUtil处理密码
- [ ] 使用Hutool的ObjectUtil进行判空

### Task 4.4: 集成Spring Security
- [ ] 创建Security配置类
- [ ] 更新UserService使用SecurityContext
- [ ] 更新Controller使用Security注解

### Task 4.5: 添加Swagger文档
- [ ] 为Controller添加Swagger注解
- [ ] 为DTO添加Swagger注解
- [ ] 配置API分组信息

---

## 六、Phase 5: 更新技术文档

### Task 5.1: 更新技术选型表
- [ ] 添加Hutool
- [ ] 更新安全框架为Spring Security
- [ ] 更新接口文档为Knife4j
- [ ] 添加框架层说明

### Task 5.2: 更新目录结构说明
- [ ] 添加juejin-framework目录说明
- [ ] 添加各starter模块说明

### Task 5.3: 添加框架使用说明
- [ ] 添加Hutool使用指南
- [ ] 添加Spring Security配置说明
- [ ] 添加Knife4j使用说明
- [ ] 添加Starter开发规范

---

## 七、版本信息

### 7.1 核心依赖版本

| 依赖 | 版本 |
|------|------|
| Spring Boot | 2.6.13 |
| Spring Cloud | 2021.0.5 |
| Spring Cloud Alibaba | 2021.0.5.0 |
| JDK | 17 |
| Hutool | 5.8.11 |
| Spring Security | 5.6.x |
| Knife4j | 4.0.0 |
| MyBatis-Plus | 3.5.3.1 |

### 7.2 Starter模块版本

所有starter模块版本跟随父项目版本：1.0.0-SNAPSHOT

---

## 八、开发规范

### 8.1 Starter开发规范

1. **命名规范**：juejin-starter-{功能名}
2. **自动配置**：必须提供spring.factories
3. **属性配置**：提供ConfigurationProperties
4. **条件装配**：使用@ConditionalOnXXX注解
5. **文档说明**：提供README.md

### 8.2 Hutool使用规范

1. **Bean转换**：使用BeanUtil.copyProperties
2. **字符串处理**：使用StrUtil
3. **集合处理**：使用CollUtil
4. **日期处理**：使用DateUtil
5. **加密解密**：使用SecureUtil
6. **JSON处理**：使用JSONUtil

### 8.3 Spring Security使用规范

1. **认证**：使用JWT Token
2. **授权**：使用RBAC模型
3. **密码**：使用BCrypt加密
4. **注解**：使用@PreAuthorize

### 8.4 Knife4j使用规范

1. **Controller**：使用@Tag注解
2. **方法**：使用@Operation注解
3. **参数**：使用@Parameter注解
4. **DTO**：使用@Schema注解
