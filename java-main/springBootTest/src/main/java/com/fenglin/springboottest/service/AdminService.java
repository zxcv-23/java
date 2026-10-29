package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.AdminInfo;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.entity.Admin;

/**
 * 后台管理员服务
 */
public interface AdminService extends IService<Admin> {

    /**
     * 后台登录：校验账号状态与 BCrypt 口令，成功返回脱敏管理员信息（含令牌）
     */
    AdminInfo login(LoginRequest request);

    /**
     * 启动时确保存在默认管理员（admin / admin123），不存在则自动创建
     */
    void ensureDefaultAdmin();
}
