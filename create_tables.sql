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
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(100) NULL,
  `phone` VARCHAR(20) NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `avatar` VARCHAR(500) NULL,
  `bio` VARCHAR(500) NULL,
  `background_image` VARCHAR(500) NULL,
  `level` TINYINT NOT NULL DEFAULT 1,
  `points` INT NOT NULL DEFAULT 0,
  `following_count` INT NOT NULL DEFAULT 0,
  `follower_count` INT NOT NULL DEFAULT 0,
  `article_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 0,
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色（0-普通用户 1-认证作者 2-管理员）',
  `last_login_time` DATETIME NULL,
  `last_login_ip` VARCHAR(50) NULL,
  `login_fail_count` INT NOT NULL DEFAULT 0,
  `lock_until_time` DATETIME NULL,
  `cancel_apply_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_nickname` (`nickname`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_points` (`points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户第三方登录表
CREATE TABLE `user_oauth` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `platform` VARCHAR(20) NOT NULL,
  `oauth_id` VARCHAR(100) NOT NULL,
  `oauth_name` VARCHAR(100) NULL,
  `oauth_avatar` VARCHAR(500) NULL,
  `bind_time` DATETIME NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_oauth_id` (`platform`, `oauth_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_user_oauth_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户社交链接表
CREATE TABLE `user_social_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `link_type` VARCHAR(20) NOT NULL,
  `link_url` VARCHAR(500) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_user_social_link_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户标签表
CREATE TABLE `user_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_user_tag_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 关注关系表
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `following_id` BIGINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `version` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_following` (`user_id`, `following_id`),
  KEY `idx_following_id` (`following_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_user_follow_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_follow_following_id` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 徽章表
CREATE TABLE `badge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) NULL,
  `icon` VARCHAR(500) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `condition_value` INT NOT NULL,
  `points` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户徽章表
CREATE TABLE `user_badge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `badge_id` BIGINT NOT NULL,
  `obtain_time` DATETIME NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`),
  KEY `idx_badge_id` (`badge_id`),
  CONSTRAINT `fk_user_badge_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_badge_badge_id` FOREIGN KEY (`badge_id`) REFERENCES `badge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户隐私设置表
CREATE TABLE `user_privacy` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `show_favorites` TINYINT NOT NULL DEFAULT 1,
  `show_following` TINYINT NOT NULL DEFAULT 1,
  `show_followers` TINYINT NOT NULL DEFAULT 1,
  `allow_stranger_message` TINYINT NOT NULL DEFAULT 1,
  `message_push_enabled` TINYINT NOT NULL DEFAULT 1,
  `do_not_disturb_start` TIME NULL,
  `do_not_disturb_end` TIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_user_privacy_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 内容模块

-- 分类表
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) NULL,
  `icon` VARCHAR(500) NULL,
  `parent_id` BIGINT NULL,
  `level` TINYINT NOT NULL DEFAULT 1,
  `article_count` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) NULL,
  `icon` VARCHAR(500) NULL,
  `parent_id` BIGINT NULL,
  `level` TINYINT NOT NULL DEFAULT 1,
  `article_count` INT NOT NULL DEFAULT 0,
  `follow_count` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_article_count` (`article_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章表
CREATE TABLE `article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `author_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `summary` VARCHAR(500) NULL,
  `cover_image` VARCHAR(500) NULL,
  `content` LONGTEXT NOT NULL,
  `content_html` LONGTEXT NULL,
  `category_id` BIGINT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `reject_reason` VARCHAR(500) NULL,
  `publish_time` DATETIME NULL,
  `scheduled_time` DATETIME NULL,
  `view_count` INT NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `favorite_count` INT NOT NULL DEFAULT 0,
  `share_count` INT NOT NULL DEFAULT 0,
  `is_top` TINYINT NOT NULL DEFAULT 0,
  `is_essence` TINYINT NOT NULL DEFAULT 0,
  `is_original` TINYINT NOT NULL DEFAULT 1,
  `source_url` VARCHAR(500) NULL,
  `copyright` TINYINT NOT NULL DEFAULT 1,
  `version` INT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章版本表
CREATE TABLE `article_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `version` INT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `content_html` LONGTEXT NULL,
  `change_summary` VARCHAR(200) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_version` (`article_id`, `version`),
  CONSTRAINT `fk_article_version_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章标签关联表
CREATE TABLE `article_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_article_tag_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_article_tag_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户关注标签表
CREATE TABLE `user_follow_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_user_follow_tag_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_follow_tag_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 专栏表
CREATE TABLE `column` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `author_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) NULL,
  `cover_image` VARCHAR(500) NULL,
  `article_count` INT NOT NULL DEFAULT 0,
  `subscribe_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_column_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 专栏文章关联表
CREATE TABLE `column_article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `column_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_column_article` (`column_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_column_article_column_id` FOREIGN KEY (`column_id`) REFERENCES `column` (`id`),
  CONSTRAINT `fk_column_article_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 草稿表
CREATE TABLE `draft` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NULL,
  `content` LONGTEXT NULL,
  `cover_image` VARCHAR(500) NULL,
  `tags` VARCHAR(500) NULL,
  `category_id` BIGINT NULL,
  `article_id` BIGINT NULL,
  `auto_save_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_draft_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_draft_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 社交模块

-- 点赞表
CREATE TABLE `like_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `version` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_like_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 收藏夹表
CREATE TABLE `favorite_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) NULL,
  `cover_image` VARCHAR(500) NULL,
  `article_count` INT NOT NULL DEFAULT 0,
  `is_default` TINYINT NOT NULL DEFAULT 0,
  `is_public` TINYINT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`user_id`, `is_default`),
  CONSTRAINT `fk_favorite_folder_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 收藏记录表
CREATE TABLE `favorite_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `folder_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_favorite_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_favorite_record_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_favorite_record_folder_id` FOREIGN KEY (`folder_id`) REFERENCES `favorite_folder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL,
  `parent_id` BIGINT NULL,
  `root_id` BIGINT NULL,
  `reply_user_id` BIGINT NULL,
  `content` VARCHAR(2000) NOT NULL,
  `like_count` INT NOT NULL DEFAULT 0,
  `reply_count` INT NOT NULL DEFAULT 0,
  `is_top` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_comment_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_comment_reply_user_id` FOREIGN KEY (`reply_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 沸点话题表
CREATE TABLE `pin_topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) NULL,
  `cover_image` VARCHAR(500) NULL,
  `pin_count` INT NOT NULL DEFAULT 0,
  `follow_count` INT NOT NULL DEFAULT 0,
  `is_hot` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_pin_count` (`pin_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 沸点表
CREATE TABLE `pin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `images` VARCHAR(2000) NULL,
  `link_url` VARCHAR(500) NULL,
  `link_title` VARCHAR(200) NULL,
  `link_cover` VARCHAR(500) NULL,
  `topic_id` BIGINT NULL,
  `like_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `share_count` INT NOT NULL DEFAULT 0,
  `is_hot` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_like_count` (`like_count`),
  CONSTRAINT `fk_pin_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_pin_topic_id` FOREIGN KEY (`topic_id`) REFERENCES `pin_topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 分享记录表
CREATE TABLE `share_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NULL,
  `target_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL,
  `share_channel` VARCHAR(20) NOT NULL,
  `share_ip` VARCHAR(50) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_share_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 消息模块

-- 通知表
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `title` VARCHAR(100) NULL,
  `content` VARCHAR(500) NOT NULL,
  `target_id` BIGINT NULL,
  `target_type` VARCHAR(20) NULL,
  `sender_id` BIGINT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `read_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`user_id`, `is_read`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_notification_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_notification_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 私信会话表
CREATE TABLE `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user1_id` BIGINT NOT NULL,
  `user2_id` BIGINT NOT NULL,
  `last_message` VARCHAR(500) NULL,
  `last_message_time` DATETIME NULL,
  `user1_unread` INT NOT NULL DEFAULT 0,
  `user2_unread` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`),
  KEY `idx_last_message_time` (`last_message_time`),
  CONSTRAINT `fk_conversation_user1_id` FOREIGN KEY (`user1_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_conversation_user2_id` FOREIGN KEY (`user2_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 私信消息表
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `content_type` TINYINT NOT NULL DEFAULT 1,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `read_time` DATETIME NULL,
  `is_recalled` TINYINT NOT NULL DEFAULT 0,
  `recall_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_message_conversation_id` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`),
  CONSTRAINT `fk_message_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 黑名单表
CREATE TABLE `blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `blocked_user_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_blocked_user_id` (`blocked_user_id`),
  CONSTRAINT `fk_blacklist_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_blacklist_blocked_user_id` FOREIGN KEY (`blocked_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 运营模块

-- 签到记录表
CREATE TABLE `sign_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `sign_date` DATE NOT NULL,
  `continuous_days` INT NOT NULL DEFAULT 1,
  `points_earned` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
  KEY `idx_sign_date` (`sign_date`),
  CONSTRAINT `fk_sign_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 任务表
CREATE TABLE `task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200) NULL,
  `type` VARCHAR(20) NOT NULL,
  `task_code` VARCHAR(50) NOT NULL,
  `condition_value` INT NOT NULL,
  `points_reward` INT NOT NULL DEFAULT 0,
  `badge_id` BIGINT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code` (`task_code`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_task_badge_id` FOREIGN KEY (`badge_id`) REFERENCES `badge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户任务表
CREATE TABLE `user_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `task_id` BIGINT NOT NULL,
  `progress` INT NOT NULL DEFAULT 0,
  `is_completed` TINYINT NOT NULL DEFAULT 0,
  `complete_time` DATETIME NULL,
  `is_claimed` TINYINT NOT NULL DEFAULT 0,
  `claim_time` DATETIME NULL,
  `task_date` DATE NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_task_date` (`user_id`, `task_id`, `task_date`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_task_date` (`task_date`),
  CONSTRAINT `fk_user_task_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_user_task_task_id` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 广告表
CREATE TABLE `advertisement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `description` VARCHAR(200) NULL,
  `image_url` VARCHAR(500) NULL,
  `link_url` VARCHAR(500) NOT NULL,
  `position` VARCHAR(20) NOT NULL,
  `ad_type` TINYINT NOT NULL DEFAULT 1,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `view_count` INT NOT NULL DEFAULT 0,
  `click_count` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户阅读历史表
CREATE TABLE `read_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `read_duration` INT NOT NULL DEFAULT 0,
  `read_progress` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_read_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_read_history_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 审核模块

-- 审核记录表
CREATE TABLE `audit_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_id` BIGINT NOT NULL,
  `target_type` VARCHAR(20) NOT NULL,
  `auditor_id` BIGINT NULL,
  `audit_type` TINYINT NOT NULL,
  `audit_result` TINYINT NOT NULL,
  `audit_reason` VARCHAR(500) NULL,
  `sensitive_words` VARCHAR(500) NULL,
  `risk_level` TINYINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_auditor_id` (`auditor_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_audit_record_auditor_id` FOREIGN KEY (`auditor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 举报表
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reporter_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  `target_type` VARCHAR(20) NOT NULL,
  `report_type` VARCHAR(20) NOT NULL,
  `report_reason` VARCHAR(500) NULL,
  `images` VARCHAR(2000) NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `handler_id` BIGINT NULL,
  `handle_result` VARCHAR(500) NULL,
  `handle_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_report_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_report_handler_id` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 敏感词表
CREATE TABLE `sensitive_word` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `word` VARCHAR(50) NOT NULL,
  `category` VARCHAR(20) NOT NULL,
  `level` TINYINT NOT NULL DEFAULT 1,
  `action` TINYINT NOT NULL DEFAULT 1,
  `replace_word` VARCHAR(50) NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 资源模块

-- 文件表
CREATE TABLE `file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `file_name` VARCHAR(200) NOT NULL,
  `file_path` VARCHAR(500) NOT NULL,
  `file_url` VARCHAR(500) NOT NULL,
  `file_size` BIGINT NOT NULL,
  `file_type` VARCHAR(50) NOT NULL,
  `file_ext` VARCHAR(20) NULL,
  `storage_type` VARCHAR(20) NOT NULL,
  `bucket_name` VARCHAR(100) NULL,
  `md5` VARCHAR(32) NULL,
  `width` INT NULL,
  `height` INT NULL,
  `thumbnail_url` VARCHAR(500) NULL,
  `audit_status` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_file_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 搜索模块

-- 搜索历史表
CREATE TABLE `search_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NULL,
  `keyword` VARCHAR(100) NOT NULL,
  `search_type` VARCHAR(20) NOT NULL DEFAULT 'all',
  `result_count` INT NOT NULL DEFAULT 0,
  `search_ip` VARCHAR(50) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_search_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 热门搜索词表
CREATE TABLE `hot_search` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `keyword` VARCHAR(100) NOT NULL,
  `search_count` INT NOT NULL DEFAULT 0,
  `stat_date` DATE NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword_date` (`keyword`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 系统配置模块

-- 系统配置表
CREATE TABLE `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL,
  `config_value` VARCHAR(2000) NOT NULL,
  `config_type` VARCHAR(20) NOT NULL,
  `description` VARCHAR(200) NULL,
  `group_name` VARCHAR(50) NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 操作日志表
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NULL,
  `username` VARCHAR(50) NULL,
  `operation_type` VARCHAR(50) NOT NULL,
  `operation_desc` VARCHAR(200) NULL,
  `request_method` VARCHAR(10) NULL,
  `request_url` VARCHAR(500) NULL,
  `request_params` TEXT NULL,
  `response_result` TEXT NULL,
  `ip` VARCHAR(50) NULL,
  `user_agent` VARCHAR(500) NULL,
  `execute_time` INT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `error_msg` TEXT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `creator` BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updater` BIGINT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_operation_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
