package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册 / 登录接口（REST）
 * 前端 Vue 项目通过 POST /api/user/register、/api/user/login 调用
 */
@RestController
@RequestMapping("/api/user")
public class RegisterController {

    @Autowired
    private UserService userService;

    /**
     * 注册新生（用户名 + 手机号 + 密码）
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("新生录入 ♥ 静候花轿");
    }

    /**
     * 登录结契（账号支持用户名或手机号）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse user = userService.login(request);
        return Result.success("契约成立 ♥ 你的灵魂，归我了", user);
    }
}
