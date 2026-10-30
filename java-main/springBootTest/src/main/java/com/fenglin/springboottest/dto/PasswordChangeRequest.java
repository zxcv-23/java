package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 个人中心 - 修改密码请求
 * 用户身份由 JWT 拦截器解析后注入，无需前端传 userId。
 */
@Data
public class PasswordChangeRequest {

    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
