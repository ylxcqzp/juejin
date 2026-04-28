# Checklist

## Project Structure
- [x] Parent POM created with correct dependency versions
- [x] Common module created with base classes
- [x] All 5 service modules created with correct structure

## Gateway Service
- [x] Gateway module created with Spring Cloud Gateway dependency
- [x] Route configuration for all services in application.yml
- [x] AuthFilter implemented with JWT validation
- [x] CORS configuration added
- [x] Rate limiting configured

## User Service
- [x] User entity class created with all fields
- [x] UserMapper interface extends BaseMapper
- [x] UserService interface and implementation created
- [x] UserController with REST endpoints created
- [x] JWT token generation implemented
- [x] Password encryption with BCrypt configured

## Content Service
- [x] Article entity class created
- [x] Tag and Category entities created
- [x] ArticleMapper with custom queries
- [x] ArticleService with CRUD operations
- [x] ArticleController with endpoints
- [x] Elasticsearch configuration (optional for now)

## Social Service
- [x] LikeRecord entity with optimistic lock
- [x] Comment entity with nested support
- [x] Redis Lua script for atomic like operations
- [x] CommentService with nested query
- [x] SocialController with endpoints

## Operation Service
- [x] SignRecord entity created
- [x] Task and UserTask entities created
- [x] Sign-in logic with consecutive days
- [x] Task completion tracking
- [x] OperationController with endpoints

## Common Module
- [x] BaseEntity with common fields (id, createTime, etc.)
- [x] Result class for API responses
- [x] BusinessException and GlobalExceptionHandler
- [x] JWT utility class
- [x] Redis utility class
- [x] PageParam and PageResult classes

## Configuration
- [x] All application.yml files configured
- [x] Database connection configured
- [x] Redis connection configured
- [x] Nacos discovery configured
- [x] MyBatis-Plus configured
