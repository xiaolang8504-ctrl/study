package com.study.api.provider;

import com.study.api.dto.response.DictLabelData;
import com.study.common.core.exception.LogicException;

import java.util.List;
import java.util.Map;

/**
 * 字典服务
 */
public interface DictProvider {

    /**
     * 检测字典数据
     */
    DictLabelData checkDict(String dictType, String dictValue) throws LogicException;

    /**
     * 键值对
     */
    Map<String, String> dictDataMap(String dictType) throws LogicException;

    /**
     * 校验键值
     */
    DictLabelData checkDictLabel(String dictType, String dictLabel) throws LogicException;

    /**
     * 获取字典列表
     */
    List<DictLabelData> checkDictLabelList(String dictType, List<String> dictLabelList) throws LogicException;

    /**
     * 校验字典列表
     */
    List<DictLabelData> checkDictValueList(String dictType, List<String> dictValueList) throws LogicException;

    /**
     * 获取字典列表
     */
    List<DictLabelData> dictLabelMap(String dictType) throws LogicException;
}
