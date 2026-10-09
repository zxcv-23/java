package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求入参（与前端 RegisterCard 表单字段对应）
 *
 * <p>只凭「夫君名讳 + 结契暗语」即可结契，不收集邮箱、手机号等阳间信息。</p>
 */
@Data
public class RegisterRequest {

    /** 夫君名讳：2~20 位，支持中文、字母、数字、下划线 */
    @NotBlank(message = "夫君还无名讳吗？")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{2,20}$",
            message = "名讳须为 2~20 位中文、字母、数字或下划线")
    private String username;

    /** 结契暗语（6~32 位） */
    @NotBlank(message = "暗语不可为空")
    @Size(min = 6, max = 32, message = "暗语须六至三十二字，方镇得住花轿")
    private String password;
}
