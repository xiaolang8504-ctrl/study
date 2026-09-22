package com.study.common.core.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 编号工具类
 */
public class SnUtils {

    /**
     * 生成前缀
     */
    public static String makePrefix(String sn) {
        String dateStr = DateTimeFormatter
                .ofPattern("yyyyMMdd")
                .format(LocalDate.now());
        return String.format("%s%s", sn, dateStr);
    }

    /**
     * 生成编号
     */
    public static String makeSn(String prefix, String lastSn) {
        int num = null == lastSn ? 0 : Integer.parseInt(lastSn.substring(prefix.length()));
        if (num != 9999) {
            num = num + 1;
        }
        return String.format("%s%04d", prefix, num);
    }

    /**
     * 生成前缀
     */
    public static String makeMonthPrefix(String sn) {
        String dateStr = DateTimeFormatter
                .ofPattern("yyyyMM")
                .format(LocalDate.now());
        return String.format("%s%s", sn, dateStr);
    }

    /**
     * 生成编号
     */
    public static String makeThreeSn(String prefix, String lastSn) {
        int num = null == lastSn ? 0 : Integer.parseInt(lastSn.substring(prefix.length()));
        if (num != 999) {
            num = num + 1;
        }
        return String.format("%s%03d", prefix, num);
    }

}
