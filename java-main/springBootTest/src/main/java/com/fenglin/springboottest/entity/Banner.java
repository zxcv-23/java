package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页轮播标语 / 主题横幅实体，对应数据库表 banner
 * <p>用于驱动前端「高级动态感」主题：首页英雄区轮播高端标语。</p>
 */
@Data
@TableName("banner")
public class Banner {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主标题 */
    private String title;

    /** 副标题/描述 */
    private String subtitle;

    /** 小标签（如上元灯会） */
    private String tag;

    /** 主题强调色（HEX），前端用于渐变光晕 */
    private String accent;

    /** 排序 */
    private Integer sort;

    /** 状态：1-展示 0-隐藏 */
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
