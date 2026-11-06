-- ============================================================
-- 鬼娘花嫁 - 数据库初始化脚本（幂等、非破坏性）
-- 数据库：dd（必须与 application-dev.properties 中的库名一致）
-- 说明：只负责「建库」。绝不 DROP DATABASE / DROP TABLE，
--       否则每次启动时都会把已注册的用户清空（历史坑）。
-- ============================================================

-- 1. 创建数据库（已存在则原样保留，数据不受影响）
CREATE DATABASE IF NOT EXISTS `dd`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `dd`;

-- 2. 表结构与示例数据由应用启动时自动完成：
--    src/main/resources/schema.sql -> CREATE TABLE IF NOT EXISTS
--    src/main/resources/data.sql   -> INSERT IGNORE
--    两者都幂等，重复启动不会重复灌入、不会清空数据。
--    config/SchemaMigration.java 会在启动后补上旧库缺失的列（不删数据）。
--
--    如需手工初始化（例如在 Navicat 或命令行里），按顺序执行：
--      mysql -uroot -p123456 -D dd < src/main/resources/schema.sql
--      mysql -uroot -p123456 -D dd < src/main/resources/data.sql
