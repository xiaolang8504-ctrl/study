package com.study.common.excel.handle;

/**
 * EasyExcel业务处理者
 */
public interface ExcelHandler<T> {

    /**
     * 业务处理
     */
    void handle(T data);
}
