package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 一条作品评论（脱敏视图）。
 * mine=true 表示是当前登录用户发的，前端据此显示「删除」按钮。
 */
@Data
public class DatingCommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long workId;

    private Long userId;

    /** 评论者昵称（脱敏，不含登录名 / 手机号） */
    private String authorName;

    /** 评论者头像 */
    private String authorAvatar;

    private String content;

    /** 是否当前用户所发 */
    private Boolean mine;

    /** 评论时间 yyyy-MM-dd HH:mm */
    private String createdAt;
}
