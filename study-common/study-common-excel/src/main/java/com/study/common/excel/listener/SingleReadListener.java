package com.study.common.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.study.common.excel.exception.ImportExcelDataException;
import com.study.common.excel.handle.ExcelHandler;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单行读取处理
 */
public class SingleReadListener<T> implements ReadListener<T> {

    /**
     * 验证器
     */
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 业务处理者
     */
    private final ExcelHandler<T> handler;

    /**
     * 构造函数
     */
    public SingleReadListener(ExcelHandler<T> handler) {
        this.handler = handler;
    }

    /**
     * 监听异常
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        String errorMessage = String.format("导入处理第%d行失败，失败原因：%s",
                context.readRowHolder().getRowIndex() + 1, exception.getMessage());
        throw new ImportExcelDataException(errorMessage);
    }

    /**
     * 调用
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        // 参数校验
        validate(data);
        // 业务处理
        handler.handle(data);
    }

    /**
     * 最后调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    /**
     * 参数校验
     */
    private static <T> void validate(T data) {
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(","));
            throw new RuntimeException(message + ";");
        }
    }
}
