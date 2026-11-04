package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.AdminPasswordChangeRequest;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.entity.Admin;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.AdminMapper;
import com.fenglin.springboottest.security.JwtService;
import com.fenglin.springboottest.service.AdminService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 后台管理员服务实现
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    /** 默认管理员账号（首次启动自动创建，登录后请立刻在「账号安全」中改密） */
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    /** BCrypt 密码加密器（带随机盐，与前台用户保持一致） */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Override
    public void ensureDefaultAdmin() {
        // 必须先补列：旧库 admin 表没有 token_version，后续任何 selectOne/selectById
        // 都会因实体字段与表结构不一致而报 Unknown column。
        ensureTokenVersionColumn();

        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, DEFAULT_ADMIN_USERNAME));
        if (count == null || count == 0) {
            Admin admin = new Admin();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setNickname("判官");
            admin.setStatus(1);
            admin.setTokenVersion(0L);
            baseMapper.insert(admin);
            log.info("已创建默认管理员 admin / admin123，请登录后尽快修改口令");
        }
    }

    /** 幂等补列：兼容从旧版 sql/guiniang.sql 导入、缺 token_version 的库 */
    private void ensureTokenVersionColumn() {
        try {
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin'",
                    Integer.class);
            // 表还不存在：schema.sql 会按含新列的完整结构创建，无需处理
            if (tableCount == null || tableCount == 0) {
                return;
            }
            Integer columnCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin' " +
                            "AND COLUMN_NAME = 'token_version'",
                    Integer.class);
            if (columnCount != null && columnCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `admin` ADD COLUMN `token_version` " +
                        "BIGINT NOT NULL DEFAULT 0 COMMENT '令牌版本'");
                log.info("admin 表已自动补齐 token_version 列");
            }
        } catch (Exception e) {
            // 不阻断启动（例如账号无 ALTER 权限），给出可手工执行的兜底方案
            log.warn("admin.token_version 自动补列失败，请手工执行 sql/fix_admin_table.sql：{}",
                    e.getMessage());
        }
    }

    @Override
    public AdminInfo login(LoginRequest request) {
        String username = request.getAccount().trim();
        String password = request.getPassword();

        if (username.isEmpty()) {
            throw new BusinessException("请填写管理员名讳");
        }

        // 1. 按账号查找管理员
        Admin admin = baseMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));

        // 2. 校验账号状态（在密码比对之前，禁用账号不泄露密码对错）
        if (admin != null && (admin.getStatus() == null || admin.getStatus() != 1)) {
            throw new BusinessException("此判官之印已被封存，无法入台");
        }

        // 3. 校验账号与口令（账号不存在与密码错误提示一致，避免账号被试探）
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("名讳或口令不匹配，判官台不认这道令牌");
        }

        // 4. 签发后台专用 JWT（scope=admin，管理员拦截器只认这种）
        Long version = tokenVersionOf(admin);
        String token = jwtService.generateAdminToken(admin.getId(), admin.getUsername(), version);
        return AdminInfo.from(admin, token);
    }

    @Override
    public AdminInfo current(Long adminId) {
        Admin admin = getById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在或已被删除");
        }
        return AdminInfo.from(admin);
    }

    @Override
    public void logout(Long adminId) {
        bumpTokenVersion(adminId);
    }

    @Override
    public AdminInfo changePassword(Long adminId, AdminPasswordChangeRequest request) {
        Admin admin = getById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在或已被删除");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new BusinessException("原口令不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), admin.getPassword())) {
            throw new BusinessException("新口令不能与原口令相同");
        }

        Long newVersion = tokenVersionOf(admin) + 1;
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setTokenVersion(newVersion);
        updateById(admin);

        // 旧 token 的版本号已不匹配，立即失效；返回新 token 让前端无缝续期
        String token = jwtService.generateAdminToken(admin.getId(), admin.getUsername(), newVersion);
        return AdminInfo.from(admin, token);
    }

    /** 自增令牌版本，使此前签发的所有后台 token 立即失效 */
    private void bumpTokenVersion(Long adminId) {
        Admin admin = getById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在或已被删除");
        }
        update(new LambdaUpdateWrapper<Admin>()
                .eq(Admin::getId, adminId)
                .set(Admin::getTokenVersion, tokenVersionOf(admin) + 1)
                .set(Admin::getUpdateTime, LocalDateTime.now()));
    }

    private Long tokenVersionOf(Admin admin) {
        return admin.getTokenVersion() == null ? 0L : admin.getTokenVersion();
    }
}
