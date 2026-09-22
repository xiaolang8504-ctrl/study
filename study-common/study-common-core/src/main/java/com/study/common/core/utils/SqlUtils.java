package com.study.common.core.utils;

/**
 * SQL 工具类
 */
public class SqlUtils {

    /**
     * 拼接 FIND_IN_SET 语句
     */
    public static String applyFindInSet(Integer value, String filed) {
        return String.format("FIND_IN_SET(%d, %s)", value, filed);
    }
}
