package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.AdminPasswordChangeRequest;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.entity.Admin;

/**
 * 后台管理员服务
 */
public interface AdminService extends IService<Admin> {

    /**
     * 后台登录：校验账号状态与 BCrypt 口令，成功返回脱敏管理员信息 + 后台 JWT
     */
    AdminInfo login(LoginRequest request);

    /**
     * 启动时确保存在默认管理员（admin / admin123），不存在则自动创建。
     * 同时幂等补齐 admin.token_version 列。
     */
    void ensureDefaultAdmin();

    /**
     * 身份自检：根据拦截器写入的管理员ID返回当前管理员信息（不含 token）
     */
    AdminInfo current(Long adminId);

    /**
     * 退出登录：自增令牌版本，使当前后台 token 立即失效
     */
    void logout(Long adminId);

    /**
     * 修改自己的口令：成功后返回携带新 token 的管理员信息，前端需替换本地会话
     */
    AdminInfo changePassword(Long adminId, AdminPasswordChangeRequest request);
}
