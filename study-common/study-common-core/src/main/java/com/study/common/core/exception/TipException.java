package com.study.common.core.exception;

import lombok.Getter;

/**
 * 文本提示异常
 */
@Getter
public class TipException extends RuntimeException {

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法,避免反序列化问题
     */
    public TipException() {
    }

    public TipException(String message) {
        this.message = message;
    }
}
