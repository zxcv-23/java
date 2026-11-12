package com.fenglin.springboottest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 私信会话摘要（左侧会话列表的一行）。
 * 会话不单独建表，这里由消息按「对方用户」归组后聚合得到。
 */
@Data
public class DatingConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 对方用户ID */
    private Long peerId;

    /** 对方昵称 */
    private String peerName;

    /** 对方头像 */
    private String peerAvatar;

    /** 对方性别：1-男 2-女 0-未知 */
    private Integer peerGender;

    /** 最后一条消息预览 */
    private String lastMessage;

    /** 最后一条消息时间 */
    private String lastTime;

    /** 我方未读条数 */
    private Integer unread;

    /** 最后一条是否为我发的 */
    private Boolean lastFromMe;
}
