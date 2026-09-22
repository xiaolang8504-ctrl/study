package com.study.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码
 */
@Getter
@AllArgsConstructor
public class ErrorCode {

    /**
     * 状态码
     */
    private int code;

    /**
     * 文本信息
     */
    private String msg;
}
