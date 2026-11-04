package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台管理员修改自己密码的请求体。
 * <p>改密成功后旧 token 立即失效，接口会返回一个携带新令牌版本的新 token，
 * 前端需替换本地会话，避免被强制登出。</p>
 */
@Data
public class AdminPasswordChangeRequest {

    /** 原口令 */
    @NotBlank(message = "请填写原口令")
    private String oldPassword;

    /** 新口令（6~40 位） */
    @NotBlank(message = "请填写新口令")
    @Size(min = 6, max = 40, message = "新口令需 6~40 位")
    private String newPassword;
}
