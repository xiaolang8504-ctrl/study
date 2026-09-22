package com.study.common.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.study.common.excel.listener.SingleReadListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * EasyExcel工具类
 */
public class EasyExcelUtils {

    /**
     * 执行
     */
    public static <T> void execute(MultipartFile file, Class excelDataClass, SingleReadListener<T> readListener) {
        if (null == file) {
            throw new RuntimeException("导入文件缺失");
        }
        try {
            EasyExcel.read(file.getInputStream(), excelDataClass, readListener).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 执行
     */
    public static <T> void execute(MultipartFile file, Class excelDataClass, ReadListener<T> readListener, Integer headerRowNumber) {
        if (null == file) {
            throw new RuntimeException("导入文件缺失");
        }
        try {
            EasyExcel.read(file.getInputStream(), excelDataClass, readListener).head(excelDataClass)
                    .headRowNumber(headerRowNumber).sheet(0).doRead();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 执行
     */
    public static <T, E> void execute(MultipartFile file,
                                      Class excelDataClass1, ReadListener<T> readListener1,
                                      Class excelDataClass2, ReadListener<E> readListener2,
                                      Integer sheet1HeaderRowNumber, Integer sheet2HeaderRowNumber) {
        if (null == file) {
            throw new RuntimeException("导入文件缺失");
        }
        try {
            ExcelReader excelReader = EasyExcel.read(file.getInputStream()).build();
            ReadSheet readSheet1 = EasyExcel.readSheet(0).headRowNumber(sheet1HeaderRowNumber)
                    .head(excelDataClass1).registerReadListener(readListener1)
                    .build();
            ReadSheet readSheet2 = EasyExcel.readSheet(1).headRowNumber(sheet2HeaderRowNumber)
                    .head(excelDataClass2).registerReadListener(readListener2)
                    .build();
            excelReader.read(readSheet1, readSheet2);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }
}
