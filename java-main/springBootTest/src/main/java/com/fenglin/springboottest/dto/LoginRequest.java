package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求入参
 *
 * <p>凭「夫君名讳（username）+ 结契暗语」结契，不使用邮箱、手机号登录。</p>
 */
@Data
public class LoginRequest {

    /** 登录账号：夫君名讳（用户名） */
    @NotBlank(message = "夫君还不肯报名吗？")
    private String account;

    /** 结契暗语 */
    @NotBlank(message = "没有暗语，如何与我结契～")
    private String password;
}
