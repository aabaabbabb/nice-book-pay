package com.nicebook.nicebookpay.config;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex, HttpServletRequest request) {
        log.warn(
                "客户端中断连接: method={}, uri={}, query={}, remoteAddr={}, message={}",
                request == null ? null : request.getMethod(),
                request == null ? null : request.getRequestURI(),
                request == null ? null : request.getQueryString(),
                request == null ? null : request.getRemoteAddr(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public void handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) throws ResponseStatusException {
        logException("状态异常", ex, request);
        throw ex;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        if (isStaticResourceRequest(request)) {
            log.warn("静态资源运行时异常: uri={}, message={}", request.getRequestURI(), ex.getMessage());
            return null;
        }
        logException("运行时异常", ex, request);
        return json(99999, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logException("业务异常", ex, request);
        return json(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {
        if (isStaticResourceRequest(request)) {
            log.warn("静态资源未处理异常: uri={}, exception={}, message={}",
                    request.getRequestURI(),
                    ex.getClass().getName(),
                    ex.getMessage());
            return null;
        }
        logException("未处理异常", ex, request);
        return json(99999, "服务器内部错误");
    }

    private boolean isStaticResourceRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri != null && (uri.startsWith("/assets/") || uri.startsWith("/webjars/") || uri.startsWith("/favicon"));
    }

    private void logException(String label, Exception ex, HttpServletRequest request) {
        if (request == null) {
            log.error("{}: {}", label, ex.getMessage(), ex);
            return;
        }
        log.error(
                "{}: method={}, uri={}, query={}, remoteAddr={}, exception={}, message={}",
                label,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr(),
                ex.getClass().getName(),
                ex.getMessage(),
                ex
        );
    }

    private ResponseEntity<Map<String, Object>> json(int code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
