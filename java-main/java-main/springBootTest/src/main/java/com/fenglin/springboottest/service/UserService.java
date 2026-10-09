package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册（用户名 + 密码，不收集邮箱、手机号）
     *
     * @param request 注册请求
     * @throws com.fenglin.springboottest.exception.BusinessException 用户名已存在或参数非法
     */
    void register(RegisterRequest request);

    /**
     * 用户登录（仅支持用户名 + 密码）
     *
     * @param request 登录请求
     * @return 登录成功后的用户信息（脱敏）
     * @throws com.fenglin.springboottest.exception.BusinessException 账号不存在、密码错误或账号被禁用
     */
    LoginResponse login(LoginRequest request);
}
