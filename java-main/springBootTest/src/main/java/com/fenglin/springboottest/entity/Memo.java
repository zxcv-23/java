package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 备忘录实体，对应数据库表 memo。
 *
 * <p>每条备忘只属于一个前台登录用户：user_id 由 JWT 拦截器解析后由服务端写入，
 * 前端不传也不能传；查询/修改/删除都会带上 user_id 条件，做到用户间数据隔离。</p>
 *
 * <p>这里采用物理删除（不设 deleted 字段），因为全局 logic-delete-field=deleted
 * 会让任何含 deleted 属性的实体自动走逻辑删除，备忘无需保留回收站。</p>
 */
@Data
@TableName("memo")
public class Memo {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 是否完成：0-待办 1-已完成 */
    private Integer done;

    /** 是否置顶：0-否 1-是 */
    private Integer pinned;

    /** 便签配色：violet / pink / gold / mint */
    private String color;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
