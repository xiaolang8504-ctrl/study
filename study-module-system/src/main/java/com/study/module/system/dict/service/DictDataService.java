package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictDataCreateReq;
import com.study.module.system.dict.entity.DictData;
import com.study.api.dto.response.DictLabelData;
import com.study.common.core.domain.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * 字典服务
 */
public interface DictDataService extends IService<DictData> {

    /**
     * 校验字典
     */
    DictData checkDictData(Integer id);

    /**
     * 校验字典
     */
    DictData checkDictData(Integer id, String dictType);

    /**
     * 是否存在字典数据
     */
    boolean isExistDictData(String dictType);

    /**
     * 校验字典
     */
    DictData checkDictData(String dictType, String dictValue, ErrorCode errorCode);

    /**
     * 构建字典信息入库
     */
    DictData buildDictData(DictDataCreateReq request);

    /**
     * 校验类型下的字典键值唯一
     */
    void existDictDataValue(String dictType, String dictValue);

    /**
     * 校验类型下的字典名唯一
     */
    DictLabelData existDictDataLabel(String dictType, String dictLabel);

    /**
     * 校验类型下的字典键值唯一
     */
    void existDictDataValue(Integer id, String dictType, String dictValue);

    /**
     * 校验类型下的字典名唯一
     */
    void existDictDataLabel(Integer id, String dictType, String dictLabel);

    /**
     * dictValue:dictLabel
     */
    Map<String, String> dictDataIdMap(String dictType);

    /**
     * 获取字典列表
     */
    List<DictLabelData> checkDictLabelList(String dictType, List<String> dictLabelList);

    /**
     * 校验字典列表
     */
    List<DictLabelData> checkDictValueList(String dictType, List<String> dictValueList);
}
