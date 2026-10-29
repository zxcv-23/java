package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.Admin;
import lombok.Data;

import java.util.UUID;

/**
 * 后台登录成功响应（脱敏：不返回密码；附带简易会话令牌）
 *
 * <p>字段命名与前端对齐：account / name / token。</p>
 */
@Data
public class AdminInfo {

    private Long id;

    /** 管理员账号 */
    private String account;

    /** 显示名（昵称） */
    private String name;

    /** 会话令牌（演示级：UUID，无服务端状态校验） */
    private String token;

    public static AdminInfo from(Admin admin) {
        AdminInfo info = new AdminInfo();
        info.setId(admin.getId());
        info.setAccount(admin.getUsername());
        info.setName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
        info.setToken(UUID.randomUUID().toString().replace("-", ""));
        return info;
    }
}
