package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.User;
import lombok.Data;

/**
 * 登录成功响应（脱敏：不返回密码等敏感字段）
 */
@Data
public class LoginResponse {

    private Long id;

    /** 夫君名讳 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 登录凭证（JWT），后续请求通过 Authorization: Bearer <token> 携带 */
    private String token;

    public static LoginResponse from(User user) {
        LoginResponse resp = new LoginResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        return resp;
    }
}
