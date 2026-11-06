-- ============================================================
-- user 表结构自愈脚本（幂等，可反复执行，不丢数据）
-- 背景：User 实体含 email / phone / gender / bio / token_version，
--       旧库缺这些列时，登录查询会报
--       Unknown column 'email' in 'field list'（前端只看到"结契失败"）。
-- 用法：mysql -uroot -p123456 < sql\fix_user_table.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `dd`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `dd`;

-- 1. 表都不存在时先按完整结构建表
CREATE TABLE IF NOT EXISTS `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50)  NOT NULL,
  `password`      VARCHAR(100) NOT NULL,
  `nickname`      VARCHAR(50)  DEFAULT NULL,
  `avatar`        VARCHAR(255) DEFAULT NULL,
  `email`         VARCHAR(100) DEFAULT NULL,
  `phone`         VARCHAR(20)  DEFAULT NULL,
  `gender`        TINYINT      DEFAULT 0,
  `bio`           VARCHAR(255) DEFAULT NULL,
  `token_version` BIGINT       NOT NULL DEFAULT 0,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. 逐个补齐缺失列（已存在则跳过，不报错、不动数据）
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user' AND COLUMN_NAME='email')=0,
  'ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) DEFAULT NULL', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user' AND COLUMN_NAME='phone')=0,
  'ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user' AND COLUMN_NAME='gender')=0,
  'ALTER TABLE `user` ADD COLUMN `gender` TINYINT DEFAULT 0', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user' AND COLUMN_NAME='bio')=0,
  'ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(255) DEFAULT NULL', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user' AND COLUMN_NAME='token_version')=0,
  'ALTER TABLE `user` ADD COLUMN `token_version` BIGINT NOT NULL DEFAULT 0', 'DO 0');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3. 结果自检：应输出 5
SELECT COUNT(*) AS required_cols
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='dd' AND TABLE_NAME='user'
  AND COLUMN_NAME IN ('email','phone','gender','bio','token_version');
