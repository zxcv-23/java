-- ============================================================
-- 备忘录表手动建表脚本（幂等，可重复执行）
-- 用法：mysql -uroot -p123456 -D dd < sql/create_memo_table.sql
-- 说明：应用每次启动也会由 schema.sql 自动执行同样的 CREATE TABLE IF NOT EXISTS，
--       这个脚本只是给「不想重启后端、想立刻生效」的场景用的。
-- ============================================================
SET NAMES utf8mb4;

-- ============================================================
-- 备忘录表（前台登录用户私有；接口挂在 /api/profile/** 下，由 JWT 拦截器按用户隔离）
-- ============================================================
CREATE TABLE IF NOT EXISTS `memo` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT       NOT NULL                COMMENT '所属用户ID（由 JWT 解析，前端不传）',
  `title`       VARCHAR(120) NOT NULL DEFAULT ''     COMMENT '标题',
  `content`     TEXT                                COMMENT '正文',
  `done`        TINYINT      NOT NULL DEFAULT 0      COMMENT '是否完成：0-待办 1-已完成',
  `pinned`      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否置顶：0-否 1-是',
  `color`       VARCHAR(20)  NOT NULL DEFAULT 'violet' COMMENT '便签配色：violet/pink/gold/mint',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_memo_user` (`user_id`),
  KEY `idx_memo_user_order` (`user_id`, `pinned`, `done`, `update_time`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='用户备忘录（按登录用户隔离）';
