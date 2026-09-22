package com.study.common.core.exception;

import com.study.common.core.domain.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class LogicException extends RuntimeException {

    /**
     * 业务错误码
     */
    private int code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法,避免反序列化问题
     */
    public LogicException() {
    }

    public LogicException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
