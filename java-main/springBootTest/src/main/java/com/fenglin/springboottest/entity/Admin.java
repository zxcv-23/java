package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员实体，对应数据库表 admin
 */
@Data
@TableName("admin")
public class Admin {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 管理员账号 */
    private String username;

    /** 登录密码（BCrypt 加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 账号状态：1-正常 0-禁用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}
