package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 姻缘阁联系方式，对应数据库表 dating_contact。
 *
 * <p>与 user 表分开存放的理由：user.phone 是注册手机号，属于账号隐私，
 * 相亲页绝不能直接暴露；这里的微信 / QQ / 手机是用户<strong>自愿公开</strong>的
 * 交友联系方式，可见性由 visible 控制：</p>
 * <ul>
 *   <li>visible=0（默认）：只对「私信过来往」的登录用户可见</li>
 *   <li>visible=1：任何登录用户都能看到</li>
 * </ul>
 */
@Data
@TableName("dating_contact")
public class DatingContact {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（唯一，一人一条） */
    private Long userId;

    /** 微信号 */
    private String wechat;

    /** QQ 号 */
    private String qq;

    /** 交友手机号（与注册手机号无关，可为空） */
    private String phone;

    /** 其他联系方式（如邮箱、小红书号） */
    private String other;

    /** 可见性：0-仅私信过来往的人可见 1-所有登录用户可见 */
    private Integer visible;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
