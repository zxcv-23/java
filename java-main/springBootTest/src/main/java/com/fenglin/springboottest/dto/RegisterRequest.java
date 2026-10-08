package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求入参（与前端 RegisterCard 表单字段对应）
 */
@Data
public class RegisterRequest {

    /** 夫君名讳 */
    @NotBlank(message = "夫君还无名讳吗？")
    private String username;

    /** 冥府信笺 */
    @NotBlank(message = "冥府信笺地址成谜了")
    @Email(message = "这信笺地址…怕不是阎王笔误？")
    private String email;

    /** 结契暗语（至少 6 位） */
    @NotBlank(message = "暗语不可为空")
    @Size(min = 6, message = "暗语至少六字，方镇得住花轿")
    private String password;
}
