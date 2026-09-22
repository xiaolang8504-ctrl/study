package com.study.common.core.utils;

import com.study.common.core.domain.TreeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 树结构工具
 */
public class TreeDataUtils {

    /**
     * 构建树结构
     */
    public static <K, V> List<TreeData<K, V>> build(List<TreeData<K, V>> nodeList, K parentId) {
        Map<K, List<TreeData<K, V>>> nodeListMap = nodeList.stream()
                .collect(Collectors.groupingBy(TreeData::getParentId));
        return recurse(nodeListMap, parentId);
    }

    /**
     * 递归构建
     */
    private static <K, V> List<TreeData<K, V>> recurse(Map<K, List<TreeData<K, V>>> nodeListMap, K parentId) {
        List<TreeData<K, V>> treeList = new ArrayList<>();
        List<TreeData<K, V>> nodeList = nodeListMap.getOrDefault(parentId, new ArrayList<>());
        for (TreeData<K, V> node : nodeList) {
            TreeData<K, V> tree = new TreeData<>();
            tree.setParentId(node.getParentId());
            tree.setParentName(node.getParentName());
            tree.setParentPath(node.getParentPath());
            tree.setKey(node.getKey());
            tree.setValue(node.getValue());
            tree.setChildren(recurse(nodeListMap, node.getKey()));
            treeList.add(tree);
        }
        return treeList;
    }
}
