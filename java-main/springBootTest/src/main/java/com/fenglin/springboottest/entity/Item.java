package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 条目/藏品实体，对应数据库表 item
 * <p>可被搜索与首页展示；通过 category_id 归属到某个分类（支持分级）。</p>
 */
@Data
@TableName("item")
public class Item {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属分类ID */
    private Long categoryId;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 图标（emoji） */
    private String emoji;

    /** 标签 */
    private String tag;

    /** 正文/详情 */
    private String content;

    /** 作者/来源 */
    private String author;

    /** 状态：1-上线 0-下线 */
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
