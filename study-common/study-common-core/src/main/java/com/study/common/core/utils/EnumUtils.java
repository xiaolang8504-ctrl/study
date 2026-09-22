package com.study.common.core.utils;

import com.study.common.core.enums.KeyValueEnum;

/**
 * 枚举工具类
 */
public class EnumUtils {

    /**
     * 是否存在指定键
     */
    private static <K, V> boolean isExist(Class<KeyValueEnum<K, V>> clazz, K key) {
        for (KeyValueEnum<K, V> enumConstant : clazz.getEnumConstants()) {
            if (key.equals(enumConstant.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取键对应的值
     */
    public static <K, V> V getValue(Class<? extends KeyValueEnum<K, V>> clazz, K key) {
        for (KeyValueEnum<K, V> enumConstant : clazz.getEnumConstants()) {
            if (key.equals(enumConstant.getKey())) {
                return enumConstant.getValue();
            }
        }
        return null;
    }
}
