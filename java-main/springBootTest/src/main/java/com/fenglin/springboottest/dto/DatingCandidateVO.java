package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 相亲阁展示用的「嘉宾」资料（对外脱敏视图）。
 *
 * <p>刻意不继承 User 实体：只保留浏览择偶必需的字段，
 * 密码、手机号、邮箱、登录用户名、tokenVersion 等一律不出接口，
 * 避免公开页面泄露注册用户隐私。</p>
 */
@Data
public class DatingCandidateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 id（前端用作心动名单的 key） */
    private Long id;

    /** 展示昵称 */
    private String nickname;

    /** 头像地址，空串表示用占位头像 */
    private String avatar;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 性别文案 */
    private String genderLabel;

    /** 个性签名 / 简介 */
    private String bio;

    /** 卡片上的小标签（如「新入阁」「姻缘簿在册」） */
    private String tag;

    /** 入阁日期 yyyy-MM-dd */
    private String joinedAt;

    /** 是否为演示嘉宾（真实库中还没有注册用户时由前端补充，此处恒为 false） */
    private Boolean demo;
}
