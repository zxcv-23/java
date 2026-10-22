package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体，对应数据库表 category
 * <p>支持多级（分级分类）：parent_id 指向父分类，0 表示一级分类。</p>
 */
@Data
@TableName("category")
public class Category {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父分类ID：0 表示一级分类 */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 分类层级：1-一级 2-二级 … */
    private Integer level;

    /** 图标（emoji） */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 分类描述 */
    private String description;

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
