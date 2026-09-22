package com.study.common.excel.handle;

import com.study.common.core.domain.Result;
import com.study.common.core.enums.SysErrorCodeConstants;
import com.study.common.excel.exception.ImportExcelDataException;
import com.study.common.core.utils.ResultUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * EXCEL异常处理
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExcelExceptionHandle {

    /**
     * 导入数据错误
     */
    @ExceptionHandler(ImportExcelDataException.class)
    public Result ExceptionHandler(ImportExcelDataException e) {
        return ResultUtils.fail(SysErrorCodeConstants.IMPORT_EXCEL_DATA_EXCEPTION.getCode(), e.getMessage());
    }
}
