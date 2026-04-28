# 代码审查报告

## 审查日期
2024年

## 审查范围
- pom.xml依赖关系
- 项目目录结构
- 核心业务代码（UserService, ArticleService, JwtUtils, AuthFilter）

---

## 问题清单

### 🔴 严重问题

#### 1. JWT密钥硬编码
**文件**: `juejin-framework/juejin-common/src/main/java/com/juejin/common/utils/JwtUtils.java`
**问题**: JWT密钥硬编码在代码中
**修复**: 改为从配置文件读取

#### 2. 依赖冗余
**文件**: `juejin-user-service/pom.xml`, `juejin-content-service/pom.xml`
**问题**: 业务服务重复引入MyBatis-Plus、Redis等依赖
**修复**: 通过juejin-common统一引入

#### 3. 缓存Key不规范
**文件**: `juejin-user-service/src/main/java/com/juejin/user/service/impl/UserServiceImpl.java`
**问题**: 直接使用字符串"user:"作为key前缀
**修复**: 使用RedisKey常量类

### 🟡 中等问题

#### 4. 事务边界不合理
**文件**: `juejin-content-service/src/main/java/com/juejin/content/service/impl/ArticleServiceImpl.java`
**问题**: getArticleById中直接更新浏览量
**修复**: 使用Redis计数器异步更新

#### 5. 未使用的导入
**文件**: `ArticleServiceImpl.java`
**问题**: Tag, TagVO, CollectionUtils导入了但未使用

#### 6. 魔法数字
**文件**: `ArticleServiceImpl.java`
**问题**: 使用数字2表示文章状态
**修复**: 使用ArticleStatusEnum枚举

### 🟢 轻微问题

#### 7. 代码风格
- 部分方法缺少JavaDoc
- 中英文注释混用

#### 8. 参数校验
- 分页参数缺少合法性校验

---

## 修复优先级

1. **P0** - JWT密钥硬编码（安全）
2. **P1** - 依赖冗余整理
3. **P1** - 缓存Key规范化
4. **P2** - 事务边界优化
5. **P3** - 代码清理和风格统一
