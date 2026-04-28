# Spring Cloud Alibaba后端项目初始化 Spec

## Why

当前项目只有前端Vue项目，需要搭建完整的Spring Cloud Alibaba微服务后端架构，包含5个核心服务：gateway-service、user-service、content-service、social-service、operation-service。

## What Changes

- 创建juejin-backend目录作为后端项目根目录
- 创建父POM统一管理依赖版本
- 创建5个微服务模块，每个模块包含完整的MVC分层结构
- 配置Spring Cloud Alibaba核心组件（Nacos、Gateway、Sentinel等）
- 集成MyBatis-Plus、Redis、RabbitMQ等中间件
- 创建统一的common模块存放公共代码

## Impact

- 新增后端项目目录结构
- 新增41张数据库表对应的实体类、Mapper、Service、Controller
- 新增微服务间通信配置（OpenFeign）
- 新增统一网关配置和认证过滤器

## ADDED Requirements

### Requirement: 项目整体架构

The system SHALL provide a complete Spring Cloud Alibaba microservices architecture with:

#### Scenario: Parent POM Configuration
- **WHEN** building the project
- **THEN** all services use consistent dependency versions (Spring Boot 2.6.13, Spring Cloud Alibaba 2021.0.5.0, JDK 17)

#### Scenario: Service Module Structure
- **WHEN** creating a new service
- **THEN** it SHALL contain: entity, mapper, service, controller, dto, vo, config packages

### Requirement: Gateway Service

The system SHALL provide a unified gateway service that:

#### Scenario: Route Configuration
- **WHEN** receiving requests
- **THEN** routes SHALL be configured for: user-service, content-service, social-service, operation-service

#### Scenario: Authentication Filter
- **WHEN** processing requests (except whitelist)
- **THEN** validates JWT token and extracts userId to header

### Requirement: User Service

The system SHALL provide user management functionality:

#### Scenario: User Registration
- **WHEN** user provides email, password, nickname
- **THEN** creates user record with encrypted password

#### Scenario: User Login
- **WHEN** user provides credentials
- **THEN** returns JWT access token and refresh token

#### Scenario: User Profile
- **WHEN** authenticated user requests profile
- **THEN** returns user information with counts

### Requirement: Content Service

The system SHALL provide content management:

#### Scenario: Article CRUD
- **WHEN** author creates/updates/deletes article
- **THEN** persists to database and updates search index

#### Scenario: Tag Management
- **WHEN** creating articles
- **THEN** supports up to 5 tags per article

### Requirement: Social Service

The system SHALL provide social interactions:

#### Scenario: Like Functionality
- **WHEN** user likes/unlikes content
- **THEN** uses Redis + Lua for atomic operations

#### Scenario: Comment System
- **WHEN** user comments on article
- **THEN** supports nested replies (2 levels max)

### Requirement: Operation Service

The system SHALL provide operational features:

#### Scenario: Sign-in System
- **WHEN** user signs in daily
- **THEN** awards points and tracks consecutive days

#### Scenario: Task System
- **WHEN** user completes tasks
- **THEN** awards points and badges

## MODIFIED Requirements

None - this is a new backend initialization.

## REMOVED Requirements

None.
