package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param request 注册请求（用户名、邮箱、密码）
     * @throws com.fenglin.springboottest.exception.BusinessException 用户名/邮箱已存在或参数非法
     */
    void register(RegisterRequest request);
}
