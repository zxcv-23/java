package com.fenglin.springboottest.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结构
 *
 * <pre>
 * {
 *   "code": 200,
 *   "message": "成功",
 *   "data": null
 * }
 * </pre>
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200 成功，400 业务/参数错误 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    public static <T> Result<T> success() {
        return of(200, "新生录入 ♥ 静候花轿", null);
    }

    public static <T> Result<T> success(String message) {
        return of(200, message, null);
    }

    public static <T> Result<T> success(String message, T data) {
        return of(200, message, data);
    }

    public static <T> Result<T> error(String message) {
        return of(400, message, null);
    }

    public static <T> Result<T> of(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
