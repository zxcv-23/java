package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录，对应数据库表 dating_like。
 *
 * <p>点赞对象是「作品」（work_id），一个用户对同一条作品只能点一次赞，
 * 由唯一索引 uk_dating_like 保证；重复点赞是幂等的，取消赞则物理删除该行。
 * 主页上的「心动」仍沿用原先前端的本地收藏（不改动），这里只服务作品点赞。</p>
 */
@Data
@TableName("dating_like")
public class DatingLike {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作品ID */
    private Long workId;

    /** 点赞用户ID */
    private Long userId;

    /** 点赞时间（由数据库默认值填充） */
    private LocalDateTime createTime;
}
