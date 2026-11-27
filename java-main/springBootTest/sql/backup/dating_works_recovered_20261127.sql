-- ============================================================
-- 姻缘阁作品广场 · 作品数据恢复（2026-11-27）
-- ============================================================
-- 背景：数据库被重置为不含姻缘阁作品的旧快照后，uploads/ 里的图片和视频还在，
--       但 dating_work / dating_work_media 表被清空，广场因此显示 0 件作品。
-- 说明：本脚本只补回作品记录，媒体文件沿用 uploads/ 中残留的原文件。
--       原作品的标题/文案/BGM 没有进入任何备份，以下为按图片内容重建的版本，
--       可在页面中删除后重新发布来替换。
-- 幂等：INSERT IGNORE + 固定主键，重复执行不会重复插入。
-- ============================================================
SET NAMES utf8mb4;
USE dd;

-- 作品 2 的作者账号（旧备份 probe_ai_01，status=0 禁用，保留作者署名且不可登录）
INSERT IGNORE INTO `user`
  (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `phone`, `gender`,
   `bio`, `token_version`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
  (2, 'probe_ai_01',
   '$2a$10$bzahpjhBihUnDSANa5Zs2um6cNTDXDLzS8mbVijDnuuJo0oHQcF1S',
   'probe_ai_01', NULL, NULL, NULL, 1, NULL, 0, 0,
   '2026-11-02 08:51:33', '2026-11-02 08:51:33', 0);

-- 作品 1：用户 1 的两张照片 + 一段视频
INSERT IGNORE INTO `dating_work`
  (`id`, `user_id`, `title`, `content`, `cover`, `bgm_url`, `bgm_name`,
   `like_count`, `comment_count`, `view_count`, `visible`, `create_time`, `update_time`)
VALUES
  (1, 1, '可爱瞬间',
   '粉色小兔、猫猫头套，还有一段随手拍的小视频。把最近的可爱留在这里啦。',
   '/uploads/work_1_2efac931191340c6ada41d91f0755ac5.jpg',
   '/bgm/summer-noon.wav', '夏日午后',
   0, 0, 0, 1, '2026-11-26 20:10:00', '2026-11-26 20:10:00');

INSERT IGNORE INTO `dating_work_media`
  (`id`, `work_id`, `media_type`, `url`, `sort_order`)
VALUES
  (1, 1, 'image', '/uploads/work_1_2efac931191340c6ada41d91f0755ac5.jpg', 0),
  (2, 1, 'image', '/uploads/work_1_f37ea41b19a3401cbb9f2a27e215e867.jpg', 1),
  (3, 1, 'video', '/uploads/video_1c6b1750973f4164800176643e6adf2f.mp4', 2);

-- 作品 2：用户 2 的一张插画
INSERT IGNORE INTO `dating_work`
  (`id`, `user_id`, `title`, `content`, `cover`, `bgm_url`, `bgm_name`,
   `like_count`, `comment_count`, `view_count`, `visible`, `create_time`, `update_time`)
VALUES
  (2, 2, '窗边的她',
   '一张很喜欢的插画，分享给也喜欢安静的人。',
   '/uploads/work_2_eded9b4cf9c34d24a9b130c315d27560.jpg',
   NULL, NULL,
   0, 0, 0, 1, '2026-11-25 19:30:00', '2026-11-25 19:30:00');

INSERT IGNORE INTO `dating_work_media`
  (`id`, `work_id`, `media_type`, `url`, `sort_order`)
VALUES
  (4, 2, 'image', '/uploads/work_2_eded9b4cf9c34d24a9b130c315d27560.jpg', 0);
