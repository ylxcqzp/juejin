# Tasks

- [x] Task 1: Create parent POM and project structure
  - [x] SubTask 1.1: Create juejin-backend directory
  - [x] SubTask 1.2: Create parent pom.xml with dependency management
  - [x] SubTask 1.3: Create common module structure

- [x] Task 2: Create gateway-service
  - [x] SubTask 2.1: Create gateway module with pom.xml
  - [x] SubTask 2.2: Configure application.yml with routes
  - [x] SubTask 2.3: Implement AuthFilter for JWT validation
  - [x] SubTask 2.4: Configure CORS and rate limiting

- [x] Task 3: Create user-service
  - [x] SubTask 3.1: Create user-service module with pom.xml
  - [x] SubTask 3.2: Configure application.yml with database and Redis
  - [x] SubTask 3.3: Create entity classes (User, UserOauth, UserFollow, etc.)
  - [x] SubTask 3.4: Create Mapper interfaces with MyBatis-Plus
  - [x] SubTask 3.5: Create Service layer with business logic
  - [x] SubTask 3.6: Create Controller with REST endpoints
  - [x] SubTask 3.7: Implement JWT token generation and validation

- [x] Task 4: Create content-service
  - [x] SubTask 4.1: Create content-service module with pom.xml
  - [x] SubTask 4.2: Configure application.yml with database and ES
  - [x] SubTask 4.3: Create entity classes (Article, Tag, Category, etc.)
  - [x] SubTask 4.4: Create Mapper interfaces
  - [x] SubTask 4.5: Create Service layer
  - [x] SubTask 4.6: Create Controller with article endpoints

- [x] Task 5: Create social-service
  - [x] SubTask 5.1: Create social-service module with pom.xml
  - [x] SubTask 5.2: Configure application.yml with database and Redis
  - [x] SubTask 5.3: Create entity classes (LikeRecord, Comment, Pin, etc.)
  - [x] SubTask 5.4: Create Mapper interfaces
  - [x] SubTask 5.5: Create Service layer with Redis Lua scripts
  - [x] SubTask 5.6: Create Controller with social endpoints

- [x] Task 6: Create operation-service
  - [x] SubTask 6.1: Create operation-service module with pom.xml
  - [x] SubTask 6.2: Configure application.yml with database and Redis
  - [x] SubTask 6.3: Create entity classes (SignRecord, Task, UserTask, etc.)
  - [x] SubTask 6.4: Create Mapper interfaces
  - [x] SubTask 6.5: Create Service layer
  - [x] SubTask 6.6: Create Controller with operation endpoints

- [x] Task 7: Create common module
  - [x] SubTask 7.1: Create common module with pom.xml
  - [x] SubTask 7.2: Create base entity class with common fields
  - [x] SubTask 7.3: Create Result wrapper class for API responses
  - [x] SubTask 7.4: Create exception classes and global handler
  - [x] SubTask 7.5: Create util classes (JWT, Redis, etc.)
  - [x] SubTask 7.6: Create DTO and VO base classes

# Task Dependencies

- Task 2 (gateway) depends on Task 1 (parent POM)
- Task 3, 4, 5, 6 (services) depend on Task 1 (parent POM) and Task 7 (common)
- Task 7 (common) depends on Task 1 (parent POM)
