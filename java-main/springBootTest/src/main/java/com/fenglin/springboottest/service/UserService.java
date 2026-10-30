package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.dto.PasswordChangeRequest;
import com.fenglin.springboottest.dto.ProfileUpdateRequest;
import com.fenglin.springboottest.dto.ProfileVO;
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

    /**
     * 获取个人中心资料
     *
     * @param userId 用户ID
     * @return 资料视图（脱敏）
     * @throws com.fenglin.springboottest.exception.BusinessException 用户不存在
     */
    ProfileVO getProfile(Long userId);

    /**
     * 修改个人资料（昵称 / 邮箱 / 手机号 / 性别 / 简介）
     *
     * @param userId  用户ID（由 JWT 拦截器注入，非前端传入）
     * @param request 修改请求
     * @throws com.fenglin.springboottest.exception.BusinessException 用户不存在或参数非法
     */
    void updateProfile(Long userId, ProfileUpdateRequest request);

    /**
     * 更新头像地址（由上传接口拿到 URL 后调用）
     *
     * @param userId    用户ID
     * @param avatarUrl 头像访问地址
     * @throws com.fenglin.springboottest.exception.BusinessException 用户不存在
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 修改密码（需校验原密码）；成功后自增令牌版本号使旧 token 失效，并返回新 token
     *
     * @param userId  用户ID（由 JWT 拦截器注入，非前端传入）
     * @param request 修改密码请求
     * @return 新的 JWT（旧 token 立即作废）
     * @throws com.fenglin.springboottest.exception.BusinessException 用户不存在、原密码错误或新密码非法
     */
    String changePassword(Long userId, PasswordChangeRequest request);

    /**
     * 登出：自增令牌版本号，使当前（及所有）token 立即失效
     *
     * @param userId 用户ID（由 JWT 拦截器注入，非前端传入）
     * @throws com.fenglin.springboottest.exception.BusinessException 用户不存在
     */
    void logout(Long userId);
}
