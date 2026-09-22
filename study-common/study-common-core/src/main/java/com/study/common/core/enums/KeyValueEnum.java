package com.study.common.core.enums;

/**
 * 键值接口
 */
public interface KeyValueEnum<K, V> {

    /**
     * 获取键
     */
    K getKey();

    /**
     * 获取值
     */
    V getValue();
}
