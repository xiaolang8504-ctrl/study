package com.study.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回结果
 */
@Getter
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 文本信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;
}
