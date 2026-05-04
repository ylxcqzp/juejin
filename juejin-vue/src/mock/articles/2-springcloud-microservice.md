# Spring Cloud Alibaba 微服务实战：从零搭建企业级架构

## 项目背景

随着业务规模的增长，单体架构的弊端日益显现。本文将带你从零搭建一个 **生产级微服务架构**，涵盖服务注册发现、配置管理、网关路由、熔断限流等核心组件。

## 架构总览

```
┌─────────────────────────────────────────────────┐
│                   Client (Web/App)               │
└───────────────────┬─────────────────────────────┘
                    │
            ┌───────▼────────┐
            │  Gateway (8080)│  ← Spring Cloud Gateway
            │  + Sentinel    │  ← 流量控制
            └───────┬────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
  ┌─────▼────┐ ┌───▼───┐ ┌───▼──────┐
  │ User     │ │Content│ │ Social   │
  │ Service  │ │Service│ │ Service  │
  │ (8081)   │ │(8082) │ │ (8083)   │
  └─────┬────┘ └───┬───┘ └───┬──────┘
        │          │         │
  ┌─────▼──────────▼─────────▼────┐
  │      Nacos (8848)            │  ← 注册中心 + 配置中心
  │      MySQL + Redis           │  ← 数据层
  └──────────────────────────────┘
```

## 1. Nacos 服务注册与发现

### 依赖配置

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <version>2021.0.5.0</version>
</dependency>
```

### 启动类配置

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.juejin.common.feign")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

## 2. Gateway 网关层

网关是整个系统的 **唯一入口**，负责：

- **路由转发**：根据路径前缀分发到对应微服务
- **JWT 认证**：解析 Bearer Token，注入 `X-User-Id` 到下游
- **CORS 跨域**：统一处理跨域请求
- **Sentinel 限流**：保护后端服务不被流量冲垮

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://juejin-user-service
          predicates:
            - Path=/api/v1/users/**,/api/v1/auth/**
        - id: content-service
          uri: lb://juejin-content-service
          predicates:
            - Path=/api/v1/articles/**,/api/v1/tags/**
```

## 3. Feign 服务间通信

```java
@FeignClient(name = "juejin-user-service", path = "/api/v1/users")
public interface UserFeignClient {

    @GetMapping("/{id}")
    Result<UserInfoVO> getUserById(@PathVariable("id") Long userId);

    @PostMapping("/{id}/points/add")
    Result<Integer> addPoints(@PathVariable("id") Long userId,
                               @RequestParam("points") Integer points);
}
```

> ⚠️ 注意：Feign 调用时务必通过 `RequestInterceptor` 自动转发 `Authorization` 和 `X-User-Id` 请求头，否则下游服务的认证过滤器会拦截请求。

## 4. Sentinel 流量控制

| 规则类型 | 说明 | 示例 |
|----------|------|------|
| QPS 限流 | 控制每秒请求数 | 登录接口限流 10 QPS |
| 线程数限流 | 控制并发线程数 | 文章详情最多 50 并发 |
| 熔断降级 | 异常比例熔断 | 错误率 > 50% 触发熔断 |
| 热点规则 | 针对特定参数限流 | 热门文章 ID 单独限流 |

## 性能指标

经过压力测试，当前架构在 4 核 8G 虚拟机上的表现：

- **单服务 QPS**：~3000（不含数据库瓶颈）
- **Gateway 转发延迟**：< 5ms（P99）
- **Nacos 服务发现延迟**：< 1s（服务下线检测）
- **Feign 调用延迟**：< 10ms（同机房内网）

![微服务架构拓扑图](https://picsum.photos/seed/springcloud/800/400)

## 总结

Spring Cloud Alibaba 全家桶为中小企业提供了一套 **开箱即用** 的微服务解决方案。配合 Nacos 和 Sentinel，可以快速构建高可用的分布式系统。
