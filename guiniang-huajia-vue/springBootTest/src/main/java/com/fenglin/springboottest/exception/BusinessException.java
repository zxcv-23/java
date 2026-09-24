package com.fenglin.springboottest.exception;

/**
 * 业务异常：用于抛出可被全局异常处理器捕获的友好提示
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
