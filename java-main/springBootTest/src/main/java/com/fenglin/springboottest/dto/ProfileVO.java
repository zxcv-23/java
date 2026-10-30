package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心 - 用户资料视图（脱敏：不含密码等敏感字段）
 */
@Data
public class ProfileVO {

    /** 用户ID */
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 个性签名 / 简介 */
    private String bio;

    /** 注册时间 */
    private LocalDateTime createTime;

    public static ProfileVO from(User user) {
        ProfileVO vo = new ProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setGender(user.getGender());
        vo.setBio(user.getBio());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
