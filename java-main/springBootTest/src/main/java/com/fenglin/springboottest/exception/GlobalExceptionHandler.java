package com.fenglin.springboottest.exception;

import com.fenglin.springboottest.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把业务异常 / 参数校验异常统一成 Result 返回给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（如用户名/邮箱已存在） */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /** @Valid 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.error(message);
    }
}
