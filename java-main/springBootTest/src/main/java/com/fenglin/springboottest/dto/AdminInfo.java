package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.Admin;
import lombok.Data;

/**
 * 后台登录成功响应（脱敏：不返回密码）
 *
 * <p>字段命名与前端对齐：account / name / token。</p>
 * <p>token 为服务端签发的 JWT（scope=admin，默认 8 小时有效），
 * 后续所有 /api/manage/** 与 /api/admin/** 请求都必须携带
 * {@code Authorization: Bearer <token>}。</p>
 */
@Data
public class AdminInfo {

    private Long id;

    /** 管理员账号 */
    private String account;

    /** 显示名（昵称） */
    private String name;

    /** 后台会话令牌（JWT） */
    private String token;

    /** 仅返回管理员基本信息，不含令牌（用于 /api/admin/me 身份自检） */
    public static AdminInfo from(Admin admin) {
        AdminInfo info = new AdminInfo();
        info.setId(admin.getId());
        info.setAccount(admin.getUsername());
        info.setName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
        return info;
    }

    /** 登录 / 改密成功时附带新签发的令牌 */
    public static AdminInfo from(Admin admin, String token) {
        AdminInfo info = from(admin);
        info.setToken(token);
        return info;
    }
}
