package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体，对应数据库表 favorite。
 * <p>一个用户对同一个条目只能收藏一次，由唯一索引 uk_favorite_user_item 保证；
 * 取消收藏直接物理删除该行（数据量小、无需逻辑删除）。</p>
 */
@Data
@TableName("favorite")
public class Favorite {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 条目ID */
    private Long itemId;

    /** 收藏时间（由数据库默认值填充） */
    private LocalDateTime createTime;
}
