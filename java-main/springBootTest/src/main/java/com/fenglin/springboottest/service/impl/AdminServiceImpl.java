package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.entity.Admin;
import com.fenglin.springboottest.exception.BusinessException;
import com.fenglin.springboottest.mapper.AdminMapper;
import com.fenglin.springboottest.service.AdminService;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 后台管理员服务实现
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    /** 默认管理员账号（首次启动自动创建，登录后建议修改） */
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    /** BCrypt 密码加密器（带随机盐，与前台用户保持一致） */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    @Override
    public void ensureDefaultAdmin() {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, DEFAULT_ADMIN_USERNAME));
        if (count == null || count == 0) {
            Admin admin = new Admin();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setNickname("判官");
            admin.setStatus(1);
            baseMapper.insert(admin);
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

        return AdminInfo.from(admin);
    }
}
