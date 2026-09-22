package com.study.common.core.domain;

import lombok.Data;

/**
 * 键值对
 */
@Data
public class KeyValue<K, V> {

    /**
     * 键
     */
    private K key;

    /**
     * 值
     */
    private V value;
}
