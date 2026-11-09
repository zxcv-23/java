package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 备忘录新增 / 修改请求。
 *
 * <p>同一个 DTO 用于 POST 与 PUT：所有字段都允许为 null（PUT 是部分更新，
 * 未传的字段保持原值）；必填校验放在 Service 层按场景判断，避免新增时
 * 无法复用 update 的部分更新语义。</p>
 *
 * <p>用户身份由 JWT 拦截器注入，不在此 DTO 中传递 userId。</p>
 */
@Data
public class MemoRequest {

    /** 标题（≤120 字） */
    @Size(max = 120, message = "标题不超过 120 字")
    private String title;

    /** 正文（≤5000 字） */
    @Size(max = 5000, message = "正文不超过 5000 字")
    private String content;

    /** 是否完成：0-待办 1-已完成 */
    @Min(value = 0, message = "完成状态只能是 0 或 1")
    @Max(value = 1, message = "完成状态只能是 0 或 1")
    private Integer done;

    /** 是否置顶：0-否 1-是 */
    @Min(value = 0, message = "置顶状态只能是 0 或 1")
    @Max(value = 1, message = "置顶状态只能是 0 或 1")
    private Integer pinned;

    /** 便签配色 */
    @Pattern(regexp = "^(violet|pink|gold|mint)$", message = "便签配色不合法")
    private String color;
}
