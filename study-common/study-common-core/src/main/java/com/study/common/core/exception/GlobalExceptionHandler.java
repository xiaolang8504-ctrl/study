package com.study.common.core.exception;

import com.study.common.core.domain.Result;
import com.study.common.core.enums.SysErrorCodeConstants;
import com.study.common.core.utils.ResultUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LogicException.class)
    public Result exceptionHandle(LogicException e) {
        return ResultUtils.fail(e.getCode(), e.getMessage());
    }

    /**
     * 执行 TipException 业务处理。
     */
    @ExceptionHandler(TipException.class)
    public Result TipException(TipException e) {
        return ResultUtils.fail(SysErrorCodeConstants.BAD_PARAMETER.getCode(), e.getMessage());
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result exceptionHandle(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ResultUtils.fail(SysErrorCodeConstants.BAD_PARAMETER.getCode(), message);
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result exceptionHandle(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        return ResultUtils.fail(SysErrorCodeConstants.BAD_PARAMETER.getCode(), violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(";")));
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(BindException.class)
    public Result exceptionHandle(BindException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return ResultUtils.fail(SysErrorCodeConstants.BAD_PARAMETER.getCode(), message);
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result ExceptionHandler(HttpRequestMethodNotSupportedException e) {
        return ResultUtils.fail(SysErrorCodeConstants.METHOD_NOT_ALLOWED, e.getMessage());
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(NullPointerException.class)
    public Result exceptionHandler(NullPointerException e) {
        e.printStackTrace();
        return ResultUtils.fail(SysErrorCodeConstants.NULL_POINTER_EXCEPTION, e.getMessage());
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(RuntimeException.class)
    public Result exceptionHandle(RuntimeException e) {
        e.printStackTrace();
        return ResultUtils.fail(SysErrorCodeConstants.RUNTIME_EXCEPTION, e.getMessage());
    }

    /**
     * 处理相关业务数据。
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandle(Exception e) {
        e.printStackTrace();
        return ResultUtils.fail(SysErrorCodeConstants.UNKNOWN, e.getMessage());
    }
}
