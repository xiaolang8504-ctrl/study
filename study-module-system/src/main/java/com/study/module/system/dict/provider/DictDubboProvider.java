package com.study.module.system.dict.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.api.dto.response.DictLabelData;
import com.study.api.provider.DictProvider;
import com.study.common.core.constants.Enable;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.dict.convert.DictConvert;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典服务
 */
@Service
public class DictDubboProvider implements DictProvider {

    @Autowired
    DictDataService dictDataService;

    @Override
    public DictLabelData checkDict(String dictType, String dictValue) throws LogicException {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictType, dictType)
                .eq(DictData::getDictValue, dictValue)
                .eq(DictData::getIsEnable, Enable.ENABLE)
                .last("LIMIT 1");
        DictData dictData = dictDataService.getOne(queryWrapper);
        if (dictData == null) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_NOT_EXIST);
        }
        return DictConvert.INSTANCE.toDictLabelData(dictData);
    }

    /**
     * 键值对
     */
    @Override
    public Map<String, String> dictDataMap(String dictType) throws LogicException {
        return dictDataService.dictDataIdMap(dictType);
    }

    /**
     * 校验键值
     */
    @Override
    public DictLabelData checkDictLabel(String dictType, String dictLabel) throws LogicException {
        return dictDataService.existDictDataLabel(dictType, dictLabel);
    }

    /**
     * 获取字典列表
     */
    @Override
    public List<DictLabelData> checkDictLabelList(String dictType, List<String> dictLabelList) throws LogicException {
        return dictDataService.checkDictLabelList(dictType, dictLabelList);
    }

    /**
     * 校验字典列表
     */
    @Override
    public List<DictLabelData> checkDictValueList(String dictType, List<String> dictValueList) throws LogicException {
        return dictDataService.checkDictValueList(dictType, dictValueList);
    }

    /**
     * 获取字典列表
     */
    @Override
    public List<DictLabelData> dictLabelMap(String dictType) throws LogicException {
        if (ObjectUtils.isEmpty(dictType)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType);
        queryWrapper.eq(DictData::getIsEnable, Enable.ENABLE);
        List<DictData> dictDataList = dictDataService.list(queryWrapper);
        return dictDataList.stream().map(DictConvert.INSTANCE::toDictLabelData).collect(Collectors.toList());
    }
}
