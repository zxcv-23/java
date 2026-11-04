-- ============================================================
-- admin 表结构自愈脚本（幂等，可反复执行，不删数据）
-- 背景：后台登录改为 JWT 鉴权后，Admin 实体新增 token_version 列。
--       旧库缺该列时，管理员 token 校验会报
--       Unknown column 'token_version' in 'field list'。
-- 正常情况下应用启动时 AdminServiceImpl 会自动补列，本脚本用于手工兜底。
-- 用法：mysql -uroot -p123456 < sql\fix_admin_table.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `guiniang`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `guiniang`;

-- 1. 表不存在时按完整结构建表
CREATE TABLE IF NOT EXISTS `admin` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`      VARCHAR(50)  NOT NULL                COMMENT '管理员账号',
  `password`      VARCHAR(100) NOT NULL                COMMENT '登录密码(BCrypt加密存储)',
  `nickname`      VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1-正常 0-禁用',
  `token_version` BIGINT       NOT NULL DEFAULT 0      COMMENT '令牌版本：改密/登出时自增使旧 token 失效',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='后台管理员表';

-- 2. 补齐缺失列（已存在则跳过，不报错、不动数据）
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA='guiniang' AND TABLE_NAME='admin' AND COLUMN_NAME='token_version')=0,
  'ALTER TABLE `admin` ADD COLUMN `token_version` BIGINT NOT NULL DEFAULT 0', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3. 结果自检：应输出 1
SELECT COUNT(*) AS required_cols
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='guiniang' AND TABLE_NAME='admin'
  AND COLUMN_NAME = 'token_version';
