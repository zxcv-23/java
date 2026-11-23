package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品评论，对应数据库表 dating_comment。
 *
 * <p>一条评论只属于一件作品，按 id 升序即发布时间顺序。
 * 作品的评论总数冗余在 dating_work.comment_count，列表页不必 count 子表。</p>
 */
@Data
@TableName("dating_comment")
public class DatingComment {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作品ID */
    private Long workId;

    /** 评论用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 评论时间（由数据库默认值填充） */
    private LocalDateTime createTime;
}
