package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 会话里的一条消息。
 * mine=true 表示这条是当前登录用户发的，前端据此决定气泡左右。
 */
@Data
public class DatingMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Boolean mine;

    /** 是否已读（对「我发的」消息有意义） */
    private Boolean read;

    /** 发送时间 yyyy-MM-dd HH:mm */
    private String createdAt;
}
