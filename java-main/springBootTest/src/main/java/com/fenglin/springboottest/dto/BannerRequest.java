package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台轮播标语新增 / 修改请求体。
 */
@Data
public class BannerRequest {

    /** 主标题（必填，对应 banner.title varchar(80)） */
    @NotBlank(message = "主标题不能为空")
    @Size(max = 80, message = "主标题最多 80 字")
    private String title;

    /** 副标题/描述 */
    @Size(max = 160, message = "副标题最多 160 字")
    private String subtitle;

    /** 小标签 */
    @Size(max = 30, message = "小标签最多 30 字")
    private String tag;

    /** 主题强调色（HEX），如 #E84A78 */
    @Size(max = 20, message = "强调色最多 20 个字符")
    private String accent;

    /** 排序 */
    private Integer sort;

    /** 状态：1-展示 0-隐藏 */
    @Min(value = 0, message = "状态只能是 0（隐藏）或 1（展示）")
    @Max(value = 1, message = "状态只能是 0（隐藏）或 1（展示）")
    private Integer status;
}
