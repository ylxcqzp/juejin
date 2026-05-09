-- ==========================================
-- 数据库：ghost-juejin
-- MySQL 版本：8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- 存储引擎：InnoDB
-- ==========================================

CREATE DATABASE IF NOT EXISTS `ghost-juejin`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `ghost-juejin`;

-- 1. 用户模块

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) NULL COMMENT '头像URL',
  `bio` VARCHAR(500) NULL COMMENT '个人简介',
  `background_image` VARCHAR(500) NULL COMMENT '背景图URL',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '等级',
  `points` INT NOT NULL DEFAULT 0 COMMENT '掘力值',
  `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '获赞数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-禁用',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-认证作者 2-管理员',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) NULL COMMENT '最后登录IP',
  `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
  `lock_until_time` DATETIME NULL COMMENT '锁定截止时间',
  `cancel_apply_time` DATETIME NULL COMMENT '注销申请时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_nickname` (`nickname`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_points` (`points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户第三方登录表
CREATE TABLE `user_oauth` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `platform` VARCHAR(20) NOT NULL COMMENT '第三方平台：github/wechat/qq',
  `oauth_id` VARCHAR(100) NOT NULL COMMENT '第三方平台用户ID',
  `oauth_name` VARCHAR(100) NULL COMMENT '第三方平台用户名',
  `oauth_avatar` VARCHAR(500) NULL COMMENT '第三方平台头像',
  `bind_time` DATETIME NOT NULL COMMENT '绑定时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_oauth_id` (`platform`, `oauth_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_user_oauth_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户第三方登录绑定表';

-- 用户社交链接表
CREATE TABLE `user_social_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `link_type` VARCHAR(20) NOT NULL COMMENT '社交平台类型：github/blog/weibo/twitter',
  `link_url` VARCHAR(500) NOT NULL COMMENT '社交链接地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_user_social_link_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户社交链接表';

-- 用户标签表
CREATE TABLE `user_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_user_tag_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户标签关联表';

-- 关注关系表
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '关注者用户ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注用户ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-取消关注 1-已关注',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_following` (`user_id`, `following_id`),
  KEY `idx_following_id` (`following_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_user_follow_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_follow_following_id` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

-- 徽章表
CREATE TABLE `badge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '徽章名称',
  `description` VARCHAR(200) NULL COMMENT '徽章描述',
  `icon` VARCHAR(500) NOT NULL COMMENT '徽章图标URL',
  `type` VARCHAR(20) NOT NULL COMMENT '徽章类型',
  `condition_value` INT NOT NULL COMMENT '达成条件值',
  `points` INT NOT NULL DEFAULT 0 COMMENT '奖励掘力值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='徽章表';

-- 用户徽章表
CREATE TABLE `user_badge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `badge_id` BIGINT NOT NULL COMMENT '徽章ID',
  `obtain_time` DATETIME NOT NULL COMMENT '获得时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`),
  KEY `idx_badge_id` (`badge_id`),
  CONSTRAINT `fk_user_badge_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_badge_badge_id` FOREIGN KEY (`badge_id`) REFERENCES `badge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户徽章关联表';

-- 用户隐私设置表
CREATE TABLE `user_privacy` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `show_favorites` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开收藏夹：0-否 1-是',
  `show_following` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开关注列表：0-否 1-是',
  `show_followers` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开粉丝列表：0-否 1-是',
  `allow_stranger_message` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许陌生人私信：0-否 1-是',
  `message_push_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启消息推送：0-否 1-是',
  `do_not_disturb_start` TIME NULL COMMENT '免打扰开始时间',
  `do_not_disturb_end` TIME NULL COMMENT '免打扰结束时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_user_privacy_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户隐私设置表';

-- 2. 内容模块

-- 分类表
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(200) NULL COMMENT '分类描述',
  `icon` VARCHAR(500) NULL COMMENT '分类图标URL',
  `parent_id` BIGINT NULL COMMENT '父分类ID',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级',
  `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- 标签表
CREATE TABLE `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `description` VARCHAR(200) NULL COMMENT '标签描述',
  `icon` VARCHAR(500) NULL COMMENT '标签图标URL',
  `parent_id` BIGINT NULL COMMENT '父标签ID',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级',
  `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数',
  `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_article_count` (`article_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 文章表（含草稿：status=0 表示草稿）
CREATE TABLE `article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `author_id` BIGINT NOT NULL COMMENT '作者ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `summary` VARCHAR(500) NULL COMMENT '摘要',
  `cover_image` VARCHAR(500) NULL COMMENT '封面图URL',
  `content` LONGTEXT NOT NULL COMMENT 'Markdown正文',
  `content_html` LONGTEXT NULL COMMENT 'HTML渲染内容',
  `category_id` BIGINT NULL COMMENT '分类ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-审核中 2-已发布 3-已驳回',
  `reject_reason` VARCHAR(500) NULL COMMENT '驳回原因',
  `publish_time` DATETIME NULL COMMENT '发布时间',
  `scheduled_time` DATETIME NULL COMMENT '定时发布时间',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `is_essence` TINYINT NOT NULL DEFAULT 0 COMMENT '是否精华：0-否 1-是',
  `is_original` TINYINT NOT NULL DEFAULT 1 COMMENT '是否原创：0-转载 1-原创',
  `source_url` VARCHAR(500) NULL COMMENT '原文链接（转载时）',
  `copyright` TINYINT NOT NULL DEFAULT 1 COMMENT '版权设置',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_article_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_article_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表（含草稿：status=0 草稿，1 审核中，2 已发布，3 已驳回）';

-- 文章版本表
CREATE TABLE `article_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `version` INT NOT NULL COMMENT '版本号',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` LONGTEXT NOT NULL COMMENT 'Markdown正文',
  `content_html` LONGTEXT NULL COMMENT 'HTML渲染内容',
  `change_summary` VARCHAR(200) NULL COMMENT '变更摘要',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_version` (`article_id`, `version`),
  CONSTRAINT `fk_article_version_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章历史版本表';

-- 文章标签关联表
CREATE TABLE `article_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_article_tag_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_article_tag_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- 用户关注标签表
CREATE TABLE `user_follow_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_user_follow_tag_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_follow_tag_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注标签关联表';

-- 专栏表
CREATE TABLE `column` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `author_id` BIGINT NOT NULL COMMENT '作者ID',
  `name` VARCHAR(100) NOT NULL COMMENT '专栏名称',
  `description` VARCHAR(500) NULL COMMENT '专栏描述',
  `cover_image` VARCHAR(500) NULL COMMENT '专栏封面图',
  `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数',
  `subscribe_count` INT NOT NULL DEFAULT 0 COMMENT '订阅数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_column_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专栏表';

-- 专栏文章关联表
CREATE TABLE `column_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `column_id` BIGINT NOT NULL COMMENT '专栏ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_column_article` (`column_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_column_article_column_id` FOREIGN KEY (`column_id`) REFERENCES `column` (`id`),
  CONSTRAINT `fk_column_article_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专栏文章关联表';

-- 3. 社交模块

-- 点赞表
CREATE TABLE `like_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_id` BIGINT NOT NULL COMMENT '目标ID（文章/评论/沸点）',
  `target_type` TINYINT NOT NULL COMMENT '目标类型：1-文章 2-评论 3-沸点',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-取消 1-已点赞',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_like_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 收藏夹表
CREATE TABLE `favorite_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收藏夹名称',
  `description` VARCHAR(200) NULL COMMENT '收藏夹描述',
  `cover_image` VARCHAR(500) NULL COMMENT '收藏夹封面',
  `article_count` INT NOT NULL DEFAULT 0 COMMENT '收藏文章数',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认收藏夹：0-否 1-是',
  `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开：0-否 1-是',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`user_id`, `is_default`),
  CONSTRAINT `fk_favorite_folder_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 收藏记录表
CREATE TABLE `favorite_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `folder_id` BIGINT NOT NULL COMMENT '收藏夹ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_favorite_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_favorite_record_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_favorite_record_folder_id` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏记录表';

-- 评论表
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `target_id` BIGINT NOT NULL COMMENT '目标ID（文章/沸点）',
  `target_type` TINYINT NOT NULL COMMENT '目标类型：1-文章 2-沸点',
  `parent_id` BIGINT NULL COMMENT '父评论ID（回复时）',
  `root_id` BIGINT NULL COMMENT '根评论ID',
  `reply_user_id` BIGINT NULL COMMENT '被回复用户ID',
  `content` VARCHAR(2000) NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` INT NOT NULL DEFAULT 0 COMMENT '回复数',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏 1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_comment_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_comment_reply_user_id` FOREIGN KEY (`reply_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 沸点话题表
CREATE TABLE `pin_topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '话题名称',
  `description` VARCHAR(200) NULL COMMENT '话题描述',
  `cover_image` VARCHAR(500) NULL COMMENT '话题封面图',
  `pin_count` INT NOT NULL DEFAULT 0 COMMENT '沸点数',
  `follow_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热门：0-否 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_pin_count` (`pin_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='沸点话题表';

-- 沸点表
CREATE TABLE `pin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '沸点内容',
  `images` VARCHAR(2000) NULL COMMENT '图片URL列表（JSON数组）',
  `link_url` VARCHAR(500) NULL COMMENT '链接URL',
  `link_title` VARCHAR(200) NULL COMMENT '链接标题',
  `link_cover` VARCHAR(500) NULL COMMENT '链接封面图',
  `topic_id` BIGINT NULL COMMENT '话题ID',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热门：0-否 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏 1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_like_count` (`like_count`),
  CONSTRAINT `fk_pin_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_pin_topic_id` FOREIGN KEY (`topic_id`) REFERENCES `pin_topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='沸点表';

-- 分享记录表
CREATE TABLE `share_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '分享用户ID（未登录可为空）',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型：1-文章 2-沸点',
  `share_channel` VARCHAR(20) NOT NULL COMMENT '分享渠道',
  `share_ip` VARCHAR(50) NULL COMMENT '分享时的IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_share_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享记录表';

-- 4. 消息模块

-- 通知表
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型：like/comment/follow/system',
  `title` VARCHAR(100) NULL COMMENT '通知标题',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `target_id` BIGINT NULL COMMENT '关联目标ID',
  `target_type` VARCHAR(20) NULL COMMENT '关联目标类型：article/comment/pin',
  `sender_id` BIGINT NULL COMMENT '触发通知的用户ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `read_time` DATETIME NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`user_id`, `is_read`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_notification_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_notification_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 私信会话表
CREATE TABLE `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user1_id` BIGINT NOT NULL COMMENT '会话用户1',
  `user2_id` BIGINT NOT NULL COMMENT '会话用户2',
  `last_message` VARCHAR(500) NULL COMMENT '最后一条消息内容',
  `last_message_time` DATETIME NULL COMMENT '最后消息时间',
  `user1_unread` INT NOT NULL DEFAULT 0 COMMENT '用户1未读数',
  `user2_unread` INT NOT NULL DEFAULT 0 COMMENT '用户2未读数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`),
  KEY `idx_last_message_time` (`last_message_time`),
  CONSTRAINT `fk_conversation_user1_id` FOREIGN KEY (`user1_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_conversation_user2_id` FOREIGN KEY (`user2_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信会话表';

-- 私信消息表
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
  `content_type` TINYINT NOT NULL DEFAULT 1 COMMENT '内容类型：1-文本 2-图片',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `read_time` DATETIME NULL COMMENT '阅读时间',
  `is_recalled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已撤回：0-否 1-是',
  `recall_time` DATETIME NULL COMMENT '撤回时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_message_conversation_id` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`),
  CONSTRAINT `fk_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- 黑名单表
CREATE TABLE `blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `blocked_user_id` BIGINT NOT NULL COMMENT '被拉黑的用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_blocked_user_id` (`blocked_user_id`),
  CONSTRAINT `fk_blacklist_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_blacklist_blocked_user_id` FOREIGN KEY (`blocked_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- 5. 运营模块

-- 签到记录表
CREATE TABLE `sign_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `sign_date` DATE NOT NULL COMMENT '签到日期',
  `continuous_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `points_earned` INT NOT NULL DEFAULT 0 COMMENT '获得掘力值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
  KEY `idx_sign_date` (`sign_date`),
  CONSTRAINT `fk_sign_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

-- 任务表
CREATE TABLE `task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '任务名称',
  `description` VARCHAR(200) NULL COMMENT '任务描述',
  `type` VARCHAR(20) NOT NULL COMMENT '任务类型：newbie/daily',
  `task_code` VARCHAR(50) NOT NULL COMMENT '任务编码',
  `condition_value` INT NOT NULL COMMENT '达成条件值',
  `points_reward` INT NOT NULL DEFAULT 0 COMMENT '奖励掘力值',
  `badge_id` BIGINT NULL COMMENT '关联徽章ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code` (`task_code`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_task_badge_id` FOREIGN KEY (`badge_id`) REFERENCES `badge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 用户任务表
CREATE TABLE `user_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '当前进度',
  `is_completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成：0-否 1-是',
  `complete_time` DATETIME NULL COMMENT '完成时间',
  `is_claimed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已领取奖励：0-否 1-是',
  `claim_time` DATETIME NULL COMMENT '领取奖励时间',
  `task_date` DATE NOT NULL COMMENT '任务日期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_task_date` (`user_id`, `task_id`, `task_date`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_task_date` (`task_date`),
  CONSTRAINT `fk_user_task_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_task_task_id` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户任务进度表';

-- 广告表
CREATE TABLE `advertisement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '广告标题',
  `description` VARCHAR(200) NULL COMMENT '广告描述',
  `image_url` VARCHAR(500) NULL COMMENT '广告图片URL',
  `link_url` VARCHAR(500) NOT NULL COMMENT '广告跳转链接',
  `position` VARCHAR(20) NOT NULL COMMENT '广告位置',
  `ad_type` TINYINT NOT NULL DEFAULT 1 COMMENT '广告类型：1-图片 2-文字',
  `start_time` DATETIME NOT NULL COMMENT '投放开始时间',
  `end_time` DATETIME NOT NULL COMMENT '投放结束时间',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '展示次数',
  `click_count` INT NOT NULL DEFAULT 0 COMMENT '点击次数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告表';

-- 用户阅读历史表
CREATE TABLE `read_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `read_duration` INT NOT NULL DEFAULT 0 COMMENT '阅读时长（秒）',
  `read_progress` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '阅读进度百分比',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_read_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_read_history_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户阅读历史表';

-- 6. 审核模块

-- 审核记录表
CREATE TABLE `audit_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_id` BIGINT NOT NULL COMMENT '审核目标ID（文章/评论/沸点）',
  `target_type` VARCHAR(20) NOT NULL COMMENT '审核目标类型：article/comment/pin',
  `auditor_id` BIGINT NULL COMMENT '审核人ID',
  `audit_type` TINYINT NOT NULL COMMENT '审核类型：1-机器审核 2-人工审核',
  `audit_result` TINYINT NOT NULL COMMENT '审核结果：1-通过 2-驳回 3-删除',
  `audit_reason` VARCHAR(500) NULL COMMENT '审核原因/驳回理由',
  `sensitive_words` VARCHAR(500) NULL COMMENT '命中的敏感词列表',
  `risk_level` TINYINT NULL COMMENT '风险等级：1-低 2-中 3-高',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_auditor_id` (`auditor_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_audit_record_auditor_id` FOREIGN KEY (`auditor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- 举报表
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_id` BIGINT NOT NULL COMMENT '被举报目标ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT '被举报目标类型',
  `report_type` VARCHAR(20) NOT NULL COMMENT '举报类型',
  `report_reason` VARCHAR(500) NULL COMMENT '举报原因',
  `images` VARCHAR(2000) NULL COMMENT '举报图片证据（JSON数组）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理 1-已处理',
  `handler_id` BIGINT NULL COMMENT '处理人ID',
  `handle_result` VARCHAR(500) NULL COMMENT '处理结果',
  `handle_time` DATETIME NULL COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_report_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_report_handler_id` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- 敏感词表
CREATE TABLE `sensitive_word` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` VARCHAR(50) NOT NULL COMMENT '敏感词',
  `category` VARCHAR(20) NOT NULL COMMENT '敏感词分类',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '敏感等级：1-低 2-中 3-高',
  `action` TINYINT NOT NULL DEFAULT 1 COMMENT '处理动作：1-替换 2-拦截 3-人工审核',
  `replace_word` VARCHAR(50) NULL COMMENT '替换词',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 7. 资源模块

-- 文件表
CREATE TABLE `file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `file_name` VARCHAR(200) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(50) NOT NULL COMMENT '文件MIME类型',
  `file_ext` VARCHAR(20) NULL COMMENT '文件扩展名',
  `storage_type` VARCHAR(20) NOT NULL COMMENT '存储类型：local/oss/cos',
  `bucket_name` VARCHAR(100) NULL COMMENT '存储桶名称',
  `md5` VARCHAR(32) NULL COMMENT '文件MD5值',
  `width` INT NULL COMMENT '图片宽度（像素）',
  `height` INT NULL COMMENT '图片高度（像素）',
  `thumbnail_url` VARCHAR(500) NULL COMMENT '缩略图URL',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-通过 2-不通过',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_file_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- 8. 搜索模块

-- 搜索历史表
CREATE TABLE `search_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '用户ID（未登录可为空）',
  `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
  `search_type` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT '搜索类型：all/article/user',
  `result_count` INT NOT NULL DEFAULT 0 COMMENT '搜索结果数',
  `search_ip` VARCHAR(50) NULL COMMENT '搜索时的IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_search_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- 热门搜索词表
CREATE TABLE `hot_search` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
  `search_count` INT NOT NULL DEFAULT 0 COMMENT '搜索次数',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword_date` (`keyword`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门搜索词统计表';

-- 9. 系统配置模块

-- 系统配置表
CREATE TABLE `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(2000) NOT NULL COMMENT '配置值',
  `config_type` VARCHAR(20) NOT NULL COMMENT '配置类型',
  `description` VARCHAR(200) NULL COMMENT '配置描述',
  `group_name` VARCHAR(50) NULL COMMENT '配置分组',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) NULL COMMENT '操作用户名',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(200) NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) NULL COMMENT '请求方法（GET/POST等）',
  `request_url` VARCHAR(500) NULL COMMENT '请求URL',
  `request_params` TEXT NULL COMMENT '请求参数',
  `response_result` TEXT NULL COMMENT '响应结果',
  `ip` VARCHAR(50) NULL COMMENT '请求IP',
  `user_agent` VARCHAR(500) NULL COMMENT '用户代理',
  `execute_time` INT NULL COMMENT '执行耗时（毫秒）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-失败 1-成功',
  `error_msg` TEXT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` BIGINT NULL COMMENT '创建人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` BIGINT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_operation_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 初始化数据

-- 初始化分类
INSERT INTO `category` (`name`, `description`, `sort_order`, `status`) VALUES
('frontend', '前端开发相关技术', 1, 1),
('backend', '后端开发相关技术', 2, 1),
('mobile', '移动端开发相关技术', 3, 1),
('ai', 'AI相关技术', 4, 1),
('tools', '开发工具和效率', 5, 1),
('lifestyle', '程序员生活', 6, 1);

-- 初始化标签
INSERT INTO `tag` (`name`, `description`, `level`, `status`) VALUES
('JavaScript', 'JavaScript相关', 1, 1),
('Vue.js', 'Vue.js框架', 1, 1),
('React', 'React框架', 1, 1),
('Java', 'Java编程语言', 1, 1),
('Spring Boot', 'Spring Boot框架', 1, 1),
('Python', 'Python编程语言', 1, 1),
('Go', 'Go编程语言', 1, 1),
('MySQL', 'MySQL数据库', 1, 1),
('Redis', 'Redis缓存', 1, 1),
('Docker', 'Docker容器', 1, 1);

-- 初始化任务
INSERT INTO `task` (`name`, `description`, `type`, `task_code`, `condition_value`, `points_reward`, `status`) VALUES
('complete_profile', '完善个人资料信息', 'newbie', 'complete_profile', 1, 5, 1),
('first_article', '发布你的第一篇技术文章', 'newbie', 'first_article', 1, 10, 1),
('first_like', '获得第一个点赞', 'newbie', 'first_like', 1, 5, 1),
('follow_users', '关注3个感兴趣的用户', 'newbie', 'follow_users', 3, 5, 1),
('daily_sign', '每日签到获得掘力值', 'daily', 'daily_sign', 1, 1, 1),
('daily_read', '每日阅读5篇文章', 'daily', 'daily_read', 5, 2, 1),
('daily_like', '每日点赞3篇文章', 'daily', 'daily_like', 3, 2, 1),
('daily_comment', '每日发表1条评论', 'daily', 'daily_comment', 1, 3, 1);

-- 初始化徽章
INSERT INTO `badge` (`name`, `description`, `icon`, `type`, `condition_value`, `points`, `status`) VALUES
('newbie', '完成注册', '/badges/newbie.png', 'register', 1, 0, 1),
('sign_7', '连续签到7天', '/badges/sign_7.png', 'sign', 7, 5, 1),
('sign_30', '连续签到30天', '/badges/sign_30.png', 'sign', 30, 20, 1),
('sign_100', '连续签到100天', '/badges/sign_100.png', 'sign', 100, 50, 1),
('first_article', '发布第一篇文章', '/badges/first_article.png', 'article', 1, 10, 1),
('view_1k', '文章阅读量突破1000', '/badges/view_1k.png', 'view', 1000, 10, 1),
('view_10k', '文章阅读量突破10000', '/badges/view_10k.png', 'view', 10000, 50, 1),
('like_100', '累计获得100个赞', '/badges/like_100.png', 'like', 100, 20, 1),
('like_1k', '累计获得1000个赞', '/badges/like_1k.png', 'like', 1000, 100, 1);
