-- ============================================================
-- 鬼娘花嫁 - 首页 / 搜索相关表（幂等，应用启动时由 spring.sql.init 自动执行）
-- 说明：CREATE TABLE IF NOT EXISTS，重复启动不会报错也不会清空数据。
-- ============================================================

-- 分类表（支持多级：父分类 -> 子分类）
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父分类ID：0 表示一级分类',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '分类名称',
  `level`       TINYINT      NOT NULL DEFAULT 1      COMMENT '分类层级：1-一级 2-二级',
  `icon`        VARCHAR(20)  DEFAULT NULL            COMMENT '图标(emoji)',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `description` VARCHAR(255) DEFAULT NULL            COMMENT '分类描述',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_parent` (`parent_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='分类表（支持多级：父分类->子分类）';

-- 条目/藏品表（可被搜索与展示）
CREATE TABLE IF NOT EXISTS `item` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT       NOT NULL DEFAULT 0      COMMENT '所属分类ID',
  `title`       VARCHAR(120) NOT NULL                COMMENT '标题',
  `description` VARCHAR(500) DEFAULT NULL            COMMENT '描述',
  `emoji`       VARCHAR(20)  DEFAULT NULL            COMMENT '图标(emoji)',
  `tag`         VARCHAR(50)  DEFAULT NULL            COMMENT '标签',
  `content`     TEXT         DEFAULT NULL            COMMENT '正文/详情',
  `author`      VARCHAR(50)  DEFAULT NULL            COMMENT '作者/来源',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-上线 0-下线',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_item_category` (`category_id`),
  KEY `idx_item_status` (`status`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='条目/藏品表（可被搜索与首页展示）';

-- 兼容：若 user 表尚未建立（已存在则 IF NOT EXISTS 跳过）
CREATE TABLE IF NOT EXISTS `user` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`     VARCHAR(50)  NOT NULL                COMMENT '夫君名讳/登录用户名',
  `password`     VARCHAR(100) NOT NULL                COMMENT '结契暗语/密码(BCrypt加密存储)',
  `nickname`     VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `avatar`       VARCHAR(255) DEFAULT NULL            COMMENT '头像地址',
  `email`        VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `phone`        VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `gender`       TINYINT      NOT NULL DEFAULT 0      COMMENT '性别：0-未知 1-男 2-女',
  `bio`          VARCHAR(255) DEFAULT NULL            COMMENT '个性签名/简介',
  `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1-正常 0-禁用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='用户表（鬼娘花嫁注册用户）';

-- 首页轮播标语 / 主题横幅（驱动前端「高级动态感」主题）
CREATE TABLE IF NOT EXISTS `banner` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`       VARCHAR(80)  NOT NULL                COMMENT '主标题',
  `subtitle`    VARCHAR(160) DEFAULT NULL            COMMENT '副标题/描述',
  `tag`         VARCHAR(30)  DEFAULT NULL            COMMENT '小标签(如上元灯会)',
  `accent`      VARCHAR(20)  DEFAULT NULL            COMMENT '主题强调色(HEX)，前端用于渐变光晕',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-展示 0-隐藏',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_banner_status` (`status`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='首页轮播标语/主题横幅';

-- 后台管理员表（与前台 user 表独立，仅用于后台管理系统登录）
CREATE TABLE IF NOT EXISTS `admin` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`     VARCHAR(50)  NOT NULL                COMMENT '管理员账号',
  `password`     VARCHAR(100) NOT NULL                COMMENT '登录密码(BCrypt加密存储)',
  `nickname`     VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1-正常 0-禁用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='后台管理员表';

-- ============================================================
-- 个人中心：用户表扩展字段（幂等迁移，仅当列不存在时添加）
-- 适用于「旧库已有 user 表、但缺少新列」的升级场景；全新库由上面的 CREATE 已包含。
-- ============================================================
DROP PROCEDURE IF EXISTS `migrate_user_profile`;
DELIMITER $$
CREATE PROCEDURE `migrate_user_profile`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'email') THEN
    ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'phone') THEN
    ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'gender') THEN
    ALTER TABLE `user` ADD COLUMN `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'bio') THEN
    ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(255) DEFAULT NULL COMMENT '个性签名/简介';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'token_version') THEN
    ALTER TABLE `user` ADD COLUMN `token_version` BIGINT NOT NULL DEFAULT 0 COMMENT '令牌版本：改密/登出时自增，使旧token失效';
  END IF;
END$$
DELIMITER ;
CALL `migrate_user_profile`();
DROP PROCEDURE IF EXISTS `migrate_user_profile`;
