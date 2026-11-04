package com.fenglin.springboottest.exception;

import com.fenglin.springboottest.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把业务异常 / 参数校验异常 / 未预料的运行时异常统一成 Result 返回给前端。
 *
 * <p>此前非业务异常（如数据库缺列、空指针）无人兜底，前端只能显示笼统的
 * 「请稍后再试」，排查必须翻后端控制台。这里补上兜底，并把异常写入日志。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    /**
     * 其余运行时异常（SQL 异常、空指针等）：记录完整堆栈，返回可读原因。
     * BusinessException 有更精确的处理器，Spring 会优先匹配它。
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("未预料的运行时异常", e);
        String reason = e.getMessage() == null || e.getMessage().isBlank()
                ? e.getClass().getSimpleName()
                : e.getMessage();
        if (reason.length() > 200) {
            reason = reason.substring(0, 200) + "…";
        }
        return Result.of(500, "服务器开小差了：" + reason, null);
    }
}
