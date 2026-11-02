-- ============================================================
-- 鬼娘花嫁 - 数据库初始化脚本
-- 数据库：guiniang
-- 说明：本文件同时作为“建库建表语句”的文本留存，
--       可直接在 MySQL 8.0 客户端执行（root 账号）。
-- ============================================================

-- 1. 创建数据库（若不存在）
CREATE DATABASE IF NOT EXISTS `guiniang`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

-- 2. 切换到目标库
USE `guiniang`;

-- 3. 创建用户表 user
--    ⚠️ 字段必须与 entity/User.java 完全一致（含 email/phone/gender/bio/token_version），
--       少一个列都会让登录查询报 Unknown column，前端表现为"结契失败"。
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`      VARCHAR(50)  NOT NULL                COMMENT '夫君名讳/登录用户名',
  `password`      VARCHAR(100) NOT NULL                COMMENT '结契暗语/密码(BCrypt加密存储)',
  `nickname`      VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `avatar`        VARCHAR(255) DEFAULT NULL            COMMENT '头像地址',
  `email`         VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `phone`         VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `gender`        TINYINT      DEFAULT 0               COMMENT '性别：0-未知 1-男 2-女',
  `bio`           VARCHAR(255) DEFAULT NULL            COMMENT '个性签名/简介',
  `token_version` BIGINT       NOT NULL DEFAULT 0      COMMENT '令牌版本：改密/登出时自增使旧 token 失效',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态：1-正常 0-禁用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='用户表（鬼娘花嫁注册用户）';
