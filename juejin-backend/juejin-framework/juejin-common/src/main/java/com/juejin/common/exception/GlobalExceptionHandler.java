package com.juejin.common.exception;

import com.juejin.common.result.ErrorCode;
import com.juejin.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    // HTTP 请求方法不支持（GET 调 POST 等）
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());
        return Result.error(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // 请求体格式错误（JSON 解析失败等）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Message not readable: {}", e.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "请求参数格式错误，请检查 JSON 格式");
    }

    // 缺少必填请求头（X-User-Id 等）
    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.warn("Missing request header: {}", e.getMessage());
        return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "缺少认证信息，请重新登录");
    }

    // 缺少必填参数
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "缺少必要参数: " + e.getParameterName());
    }

    // 访问不存在的路径（需配置 spring.mvc.throw-exception-if-no-handler-found=true）
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("No handler found: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND);
    }

    // 兜底：未预期的异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Internal server error", e);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }

}
