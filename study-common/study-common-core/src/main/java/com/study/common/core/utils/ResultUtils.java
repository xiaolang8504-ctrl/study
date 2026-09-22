package com.study.common.core.utils;

import com.study.common.core.domain.ErrorCode;
import com.study.common.core.domain.Result;
import com.study.common.core.enums.SysErrorCodeConstants;

/**
 * 返回结果工具
 */
public class ResultUtils {

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SysErrorCodeConstants.SUCCESS.getCode(), SysErrorCodeConstants.SUCCESS.getMsg(), data);
    }

    /**
     * 执行 fail 业务处理。
     */
    public static <T> Result<T> fail(ErrorCode codeEnum) {
        return fail(codeEnum, null);
    }

    /**
     * 执行 fail 业务处理。
     */
    public static <T> Result<T> fail(ErrorCode codeEnum, T data) {
        return new Result<>(codeEnum.getCode(), codeEnum.getMsg(), data);
    }

    /**
     * 执行 fail 业务处理。
     */
    public static <T> Result<T> fail(int code, String msg) {
        return fail(code, msg, null);
    }

    /**
     * 执行 fail 业务处理。
     */
    public static <T> Result<T> fail(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}
