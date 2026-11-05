package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台分类新增 / 修改请求体。
 * <p>修改时未传（null）的字段保持原值，传空串才会清空。</p>
 */
@Data
public class CategoryRequest {

    /** 父分类ID：0 或 null 表示一级分类 */
    private Long parentId;

    /** 分类名称（必填，对应 category.name varchar(50)） */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最多 50 字")
    private String name;

    /** 图标 emoji */
    @Size(max = 20, message = "图标最多 20 个字符")
    private String icon;

    /** 排序，默认 0 */
    private Integer sort;

    /** 分类描述 */
    @Size(max = 255, message = "分类描述最多 255 字")
    private String description;

    /** 层级：通常由父分类自动推导，也可显式指定 */
    @Min(value = 1, message = "层级至少为 1")
    @Max(value = 3, message = "层级最多为 3")
    private Integer level;
}
