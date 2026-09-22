package com.study.common.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 树结构响应数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TreeData<K, V> extends KeyValue<K, V> {

    /**
     * 父级ID
     */
    private K parentId;

    /**
     * 父级名称
     */
    private String parentName;

    /**
     * 父级全路径
     */
    private String parentPath;

    /**
     * 键
     */
    private K key;

    /**
     * 值
     */
    private V value;

    /**
     * 子选项
     */
    private List<TreeData<K, V>> children;
}
