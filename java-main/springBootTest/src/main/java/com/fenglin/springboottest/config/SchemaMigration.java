package com.fenglin.springboottest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库结构自愈（幂等迁移）。
 *
 * <p>背景：{@code schema.sql} 里的 {@code CREATE TABLE IF NOT EXISTS} 只能管到「全新库」，
 * 旧库一旦缺列，{@code spring.sql.init} 不会帮我们补。以前靠 DELIMITER 存储过程补 user 表，
 * 但 Spring 的 SQL 脚本执行器不支持 DELIMITER，那段脚本每次启动都会静默失败
 * （被 {@code continue-on-error=true} 吞掉），于是留下了「注册成功但登录 500」的坑。</p>
 *
 * <p>这里在应用启动完成后，用 information_schema 逐列检查，缺什么补什么：
 * 不删表、不删数据、不覆盖已有列，失败只记日志不阻断启动。
 * 全新库由 schema.sql 一次性建好，这里基本是空跑。</p>
 */
@Component
public class SchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

    private final JdbcTemplate jdbc;

    public SchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 兜底：即使 schema.sql 因故没被执行（脚本被跳过 / 全新空库），
        // 也保证用户表存在，注册接口不会因为「表不存在」而丢数据。
        // CREATE TABLE IF NOT EXISTS 不会覆盖或清空已有数据。
        ensureUserTable();

        // 条目浏览量（本次新增功能）
        ensureColumn("item", "view_count",
                "ALTER TABLE `item` ADD COLUMN `view_count` BIGINT NOT NULL DEFAULT 0 COMMENT '浏览次数'");

        // 个人中心扩展字段：旧库缺这些列会导致登录查询报 Unknown column
        ensureColumn("user", "email",
                "ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱'");
        ensureColumn("user", "phone",
                "ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号'");
        ensureColumn("user", "gender",
                "ALTER TABLE `user` ADD COLUMN `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女'");
        ensureColumn("user", "bio",
                "ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(255) DEFAULT NULL COMMENT '个性签名/简介'");
        ensureColumn("user", "token_version",
                "ALTER TABLE `user` ADD COLUMN `token_version` BIGINT NOT NULL DEFAULT 0 COMMENT '令牌版本'");
        ensureColumn("admin", "token_version",
                "ALTER TABLE `admin` ADD COLUMN `token_version` BIGINT NOT NULL DEFAULT 0 COMMENT '令牌版本'");

        ensureFavoriteTable();
    }

    /** 列不存在才执行 ALTER；已存在直接跳过 */
    private void ensureColumn(String table, String column, String ddl) {
        try {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class, table, column);
            if (count != null && count == 0) {
                jdbc.execute(ddl);
                log.info("数据库迁移：{} 表已补齐 {} 列", table, column);
            }
        } catch (Exception e) {
            log.warn("数据库迁移：检查/补齐 {}.{} 失败（不阻断启动）：{}", table, column, e.getMessage());
        }
    }

    /** 用户表兜底：IF NOT EXISTS 建表，已有用户数据一律保留 */
    private void ensureUserTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS `user` (" +
                    "`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID'," +
                    "`username` VARCHAR(50) NOT NULL COMMENT '登录用户名'," +
                    "`password` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码'," +
                    "`nickname` VARCHAR(50) DEFAULT NULL," +
                    "`avatar` VARCHAR(255) DEFAULT NULL," +
                    "`email` VARCHAR(100) DEFAULT NULL," +
                    "`phone` VARCHAR(20) DEFAULT NULL," +
                    "`gender` TINYINT NOT NULL DEFAULT 0," +
                    "`bio` VARCHAR(255) DEFAULT NULL," +
                    "`token_version` BIGINT NOT NULL DEFAULT 0," +
                    "`status` TINYINT NOT NULL DEFAULT 1," +
                    "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "`deleted` TINYINT NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_username` (`username`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表'");
        } catch (Exception e) {
            log.warn("数据库迁移：确保 user 表存在失败（不阻断启动）：{}", e.getMessage());
        }
    }

    /** 收藏表：CREATE TABLE IF NOT EXISTS 自身即幂等 */
    private void ensureFavoriteTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS `favorite` (" +
                    "`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID'," +
                    "`user_id` BIGINT NOT NULL COMMENT '用户ID'," +
                    "`item_id` BIGINT NOT NULL COMMENT '条目ID'," +
                    "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间'," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_favorite_user_item` (`user_id`,`item_id`)," +
                    "KEY `idx_favorite_item` (`item_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户收藏表'");
        } catch (Exception e) {
            log.warn("数据库迁移：创建 favorite 表失败（不阻断启动）：{}", e.getMessage());
        }
    }
}
