package com.study.common.core.utils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件工具
 */
public class FileUtils {

    private static String pattern = "yyyyMMdd";

    /**
     * 生成指定格式的目录名称(日期格式)
     */
    public static String genDateMkdir(String format) {
        return File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern(format)) + File.separator;
    }

    /**
     * 生成相对路径
     */
    public static String genTempPath(String uploadType) {
        return File.separator + uploadType + FileUtils.genDateMkdir(pattern);
    }
}
