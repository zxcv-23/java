package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库表 user
 */
@Data
@TableName("user")
public class User {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 夫君名讳 / 登录用户名 */
    private String username;

    /** 冥府信笺 / 邮箱 */
    private String email;

    /** 结契暗语 / 密码（BCrypt 加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 账号状态：1-正常 0-禁用 */
    private Integer status;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入、更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除 1-已删除（MyBatis-Plus 自动处理） */
    @TableLogic
    private Integer deleted;
}
