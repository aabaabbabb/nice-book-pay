package com.nicebook.nicebookpay.config;

import com.nicebook.nicebookpay.utils.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<?>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception", ex);
        return json(Response.fail(99999, ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<?>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage(), ex);
        return json(Response.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return json(Response.fail(99999, "server internal error: " + ex.getClass().getSimpleName()));
    }

    private ResponseEntity<Response<?>> json(Response<?> body) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
