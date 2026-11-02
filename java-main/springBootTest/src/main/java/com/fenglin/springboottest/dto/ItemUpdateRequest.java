package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台修改条目请求体
 *
 * <p>字段长度与 {@code item} 表列宽一一对应，避免写库时被 MySQL 截断。</p>
 */
@Data
public class ItemUpdateRequest {

    /** 标题（必填，对应 item.title varchar(120)） */
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题最多 120 字")
    private String title;

    /** 描述（对应 item.description varchar(500)） */
    @Size(max = 500, message = "描述最多 500 字")
    private String description;

    /** 图标 emoji（对应 item.emoji varchar(20)） */
    @Size(max = 20, message = "图标最多 20 个字符")
    private String emoji;

    /** 标签（对应 item.tag varchar(50)） */
    @Size(max = 50, message = "标签最多 50 字")
    private String tag;

    /** 正文/详情（对应 item.content text） */
    @Size(max = 20000, message = "正文过长（最多 20000 字）")
    private String content;

    /** 作者/来源（对应 item.author varchar(50)） */
    @Size(max = 50, message = "作者最多 50 字")
    private String author;

    /** 所属分类ID；为空表示未分类 */
    private Long categoryId;

    /** 状态：1-上线 0-下线；为空表示保持原状态 */
    @Min(value = 0, message = "状态只能是 0（下线）或 1（上线）")
    @Max(value = 1, message = "状态只能是 0（下线）或 1（上线）")
    private Integer status;
}
