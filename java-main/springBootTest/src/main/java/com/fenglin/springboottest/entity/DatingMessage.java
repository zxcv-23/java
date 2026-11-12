package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内私信（留言式聊天），对应数据库表 dating_message。
 *
 * <p>设计成「两个用户之间的一条会话」：每行是一条消息，from_user_id 发给 to_user_id。
 * 会话不单独建表，查询时按 (least(from,to), greatest(from,to)) 这对用户归组，
 * 这样无需额外的会话表也能得到「和谁聊过、最后一句是什么」。</p>
 *
 * <p>「是否互相私信过」是联系方式可见性的判定依据：只要 A 给 B 发过消息、
 * 或 B 给 A 发过消息，双方就算「有往来」，可以看到对方的联系方式。</p>
 */
@Data
@TableName("dating_message")
public class DatingMessage {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者用户ID */
    private Long fromUserId;

    /** 接收者用户ID */
    private Long toUserId;

    /** 消息正文 */
    private String content;

    /** 是否已读：1-已读 0-未读（接收方视角） */
    private Integer isRead;

    /** 发送时间（由数据库默认值填充） */
    private LocalDateTime createTime;
}
