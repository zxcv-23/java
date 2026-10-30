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

    /** 结契暗语 / 密码（BCrypt 加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 个性签名 / 简介 */
    private String bio;

    /** 令牌版本：修改密码 / 登出时自增，使旧 token 立即失效 */
    private Long tokenVersion;

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
