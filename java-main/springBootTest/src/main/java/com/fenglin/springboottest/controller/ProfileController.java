package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.PasswordChangeRequest;
import com.fenglin.springboottest.dto.ProfileUpdateRequest;
import com.fenglin.springboottest.dto.ProfileVO;
import com.fenglin.springboottest.security.JwtInterceptor;
import com.fenglin.springboottest.service.FileUploadService;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人中心接口（REST）
 * 所有接口受 JwtInterceptor 保护，用户身份从 Authorization 头解析后注入，
 * 通过 @RequestAttribute 获取，前端无需（也不应）传递 userId。
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 获取个人资料
     */
    @GetMapping
    public Result<ProfileVO> getProfile(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 修改个人资料（昵称 / 邮箱 / 手机号 / 性别 / 简介）
     */
    @PutMapping
    public Result<Void> updateProfile(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(userId, request);
        return Result.success("资料已更新 ♥");
    }

    /**
     * 上传头像（图片），成功后自动更新用户头像地址
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file, "avatar_" + userId);
        userService.updateAvatar(userId, url);
        return Result.success("头像已更新", url);
    }

    /**
     * 修改密码（需校验原密码）；成功后旧 token 失效并返回新 token
     */
    @PostMapping("/password")
    public Result<String> changePassword(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        String newToken = userService.changePassword(userId, request);
        return Result.success("密码已修改，请牢记新的结契暗语", newToken);
    }

    /**
     * 登出：使当前 token 立即失效
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestAttribute(JwtInterceptor.ATTR_USER_ID) Long userId) {
        userService.logout(userId);
        return Result.success("已退出登录 ♥");
    }
}
