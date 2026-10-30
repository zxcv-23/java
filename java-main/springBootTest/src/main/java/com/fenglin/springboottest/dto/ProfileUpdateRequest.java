package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人中心 - 修改资料请求
 * 用户身份由 JWT 拦截器解析后注入，无需前端传 userId。
 *
 * 校验说明：昵称/邮箱/手机/签名均为「部分更新」字段，未传（null）时不校验；
 * 一旦传入则按下列约束校验。性别同理，仅在非 null 时校验范围。
 */
@Data
public class ProfileUpdateRequest {

    /** 昵称（≤30 字） */
    @Size(max = 30, message = "昵称不超过 30 字")
    private String nickname;

    /** 邮箱（可选，填了才校验格式） */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱过长")
    private String email;

    /** 手机号（中国大陆 11 位） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    @Min(value = 0, message = "性别取值不合法（0-未知 1-男 2-女）")
    @Max(value = 2, message = "性别取值不合法（0-未知 1-男 2-女）")
    private Integer gender;

    /** 个性签名 / 简介（≤200 字） */
    @Size(max = 200, message = "个性签名不超过 200 字")
    private String bio;
}
