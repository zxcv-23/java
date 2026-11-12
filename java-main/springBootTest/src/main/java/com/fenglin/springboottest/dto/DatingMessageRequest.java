package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 发送私信请求体 */
@Data
public class DatingMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "说点什么吧，哪怕一句你好")
    @Size(max = 1000, message = "单条消息最多 1000 个字")
    private String content;
}
