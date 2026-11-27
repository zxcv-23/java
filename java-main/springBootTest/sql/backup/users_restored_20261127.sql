-- ============================================================
-- 姻缘阁 / 主站用户账号恢复（2026-11-27）
-- ============================================================
-- 背景：数据库被重置为旧快照后，user 表只剩当天新注册的 hh；历史上出现过的
--       ff / probe_ai_01 / fff / zhangsan 都从库里消失，作品作者也会显示「已注销用户」。
-- 来源：sql/backup/user_backup_20261102.sql、user_backup_20261126_before_restore.sql、
--       sql/dd.sql、git 历史中的旧 dump，以及当前库中的 hh。
-- 策略：INSERT IGNORE + 固定主键，重复执行不会覆盖现有账号；status=1 让它们重新出现在姻缘簿。
-- 注意：这些账号的 BCrypt 密码来自历史备份，登录后请到「个人中心」自行修改。
-- ============================================================
SET NAMES utf8mb4;
USE dd;

INSERT IGNORE INTO `user`
  (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `phone`, `gender`,
   `bio`, `token_version`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
  -- 当前账号，保留原 id=1 和原密码
  (1, 'hh', '$2a$10$n9XqAfBbHSNsU.5b6o2X2eSn5WI1yCF/Std80hcgykUvxQQT7u1ce',
   'hh', NULL, NULL, NULL, 0, NULL, 0, 1,
   '2026-11-27 13:57:55', '2026-11-27 13:57:55', 0),

  -- probe_ai_01：id=2 为作品 2 的原作者，保留该 id，恢复为可登录
  (2, 'probe_ai_01', '$2a$10$bzahpjhBihUnDSANa5Zs2um6cNTDXDLzS8mbVijDnuuJo0oHQcF1S',
   'probe_ai_01', NULL, NULL, NULL, 1, NULL, 0, 1,
   '2026-11-02 08:51:33', '2026-11-02 08:51:33', 0),

  -- ff：取 2026-11-26 备份中最新的密码
  (3, 'ff', '$2a$10$GA7CGY512Tqbf1xsFCbNReuKNJ6FtKPR2wgB72ge9TqujhkxBwLSm',
   'ff', NULL, NULL, NULL, 0, NULL, 0, 1,
   '2026-11-26 14:03:45', '2026-11-26 14:03:45', 0),

  -- fff：来自 sql/dd.sql
  (4, 'fff', '$2a$10$i88SxblF7/5se1l5x0HBbOo3WVV6HgamUw2DoSwcc8K/wTRDlWDaO',
   'fff', NULL, NULL, NULL, 0, NULL, 0, 1,
   '2026-11-11 16:11:21', '2026-11-11 16:11:21', 0),

  -- zhangsan：git 历史中的旧账号
  (5, 'zhangsan', '$2a$10$MKEQzV7JBJ5cJspJw.MA8OlJCKF9bE6Bj3fmV/4.dPvVHcxGgINki',
   'zhangsan', NULL, NULL, NULL, 1, NULL, 0, 1,
   '2026-10-09 14:50:51', '2026-10-09 14:50:51', 0);
