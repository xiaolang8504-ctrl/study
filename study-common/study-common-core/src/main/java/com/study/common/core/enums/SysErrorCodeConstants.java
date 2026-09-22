package com.study.common.core.enums;

import com.study.common.core.domain.ErrorCode;

/**
 * 系统错误码枚举常量
 */
public interface SysErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(0, "成功");
    ErrorCode SYSTEM_ERROR = new ErrorCode(1000001, "系统内部错误");
    ErrorCode UNKNOWN = new ErrorCode(1000002, "未知错误");
    ErrorCode NOT_FOUND = new ErrorCode(1000003, "请求未找到");
    ErrorCode BAD_PARAMETER = new ErrorCode(1000004, "请求参数不正确");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(1000005, "请求方法不正确");
    ErrorCode RUNTIME_EXCEPTION = new ErrorCode(1000006, "运行时异常");
    ErrorCode NULL_POINTER_EXCEPTION = new ErrorCode(1000007, "空指针异常");
    ErrorCode IMPORT_EXCEL_DATA_EXCEPTION = new ErrorCode(1000008, "导入数据失败");
    ErrorCode INVOKE_SERVICE_FAILED = new ErrorCode(1000009, "调用远程服务失败，请稍后尝试。");
}
