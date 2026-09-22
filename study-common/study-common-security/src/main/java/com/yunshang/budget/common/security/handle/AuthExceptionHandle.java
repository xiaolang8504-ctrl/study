package com.yunshang.budget.common.security.handle;

import com.study.common.core.domain.Result;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.utils.ResultUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 权限异常处理
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthExceptionHandle {

    /**
     * spring security 无权限
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public Result accessDeniedException() {
        return ResultUtils.fail(ErrorCodeConstants.ACCESS_DENIED);
    }
}