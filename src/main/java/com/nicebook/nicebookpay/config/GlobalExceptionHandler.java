package com.nicebook.nicebookpay.config;

import com.nicebook.nicebookpay.utils.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有运行时异常 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Response<?> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception", ex);
        return Response.fail(99999, ex.getMessage());
    }

    /**
     * 处理业务异常 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Response<?> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage(), ex);
        return Response.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Response<?> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return Response.fail(99999, "服务器内部错误");
    }
}
