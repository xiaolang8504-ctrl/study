package com.study.module.system.dict.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.convert.DictConvert;
import com.study.module.system.dict.dto.request.DictDataCreateReq;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictService;
import com.study.api.dto.response.DictLabelData;
import com.study.common.core.constants.Enable;
import com.study.common.core.domain.ErrorCode;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字典数据服务
 */
@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    @Autowired
    DictService dictService;

    /**
     * 校验字典数据
     */
    @Override
    public DictData checkDictData(Integer id) {
        DictData dictData = this.getById(id);
        if (dictData == null) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_NOT_EXIST);
        }
        return dictData;
    }

    /**
     * 校验字典
     */
    @Override
    public DictData checkDictData(Integer id, String dictType) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(id), DictData::getId, id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        DictData dictData = this.getOne(queryWrapper);
        if (dictData == null) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_NOT_EXIST);
        }
        return dictData;
    }

    /**
     * 是否存在字典数据
     */
    @Override
    public boolean isExistDictData(String dictType) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 校验字典
     */
    @Override
    public DictData checkDictData(String dictType, String dictValue, ErrorCode errorCode) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType);
        queryWrapper.eq(DictData::getDictValue, dictValue);
        DictData dictData = this.getOne(queryWrapper);
        if (dictData == null || dictData.getIsEnable() == Enable.DISABLE) {
            throw new LogicException(errorCode);
        }
        return dictData;
    }

    /**
     * 构建字典数据信息入库
     */
    @Override
    public DictData buildDictData(DictDataCreateReq request) {
        return DictConvert.INSTANCE.toDictData(request);
    }

    /**
     * 校验类型下的字典数据名唯一
     */
    @Override
    public void existDictDataValue(String dictType, String dictValue) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictValue), DictData::getDictValue, dictValue);
        queryWrapper.last("LIMIT 1");
        DictData dictData = this.getOne(queryWrapper);
        if (dictData != null) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_VALUE_EXIST);
        }
    }

    /**
     * 校验类型下的字典名唯一
     */
    @Override
    public DictLabelData existDictDataLabel(String dictType, String dictLabel) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictLabel), DictData::getDictLabel, dictLabel);
        queryWrapper.last("LIMIT 1");
        DictData dictData = this.getOne(queryWrapper);
        return DictConvert.INSTANCE.toDictLabelData(dictData);
    }

    /**
     * 校验类型下的字典数据名唯一
     */
    @Override
    public void existDictDataValue(Integer id, String dictType, String dictValue) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictValue), DictData::getDictValue, dictValue);
        queryWrapper.last("LIMIT 1");
        DictData dictData = this.getOne(queryWrapper);
        if (dictData != null && !Objects.equals(dictData.getId(), id)) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_VALUE_EXIST);
        }
    }

    /**
     * 校验类型下的字典名唯一
     */
    @Override
    public void existDictDataLabel(Integer id, String dictType, String dictLabel) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictLabel), DictData::getDictLabel, dictLabel);
        queryWrapper.last("LIMIT 1");
        DictData dictData = this.getOne(queryWrapper);
        if (dictData != null && !Objects.equals(dictData.getId(), id)) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_LABEL_EXIST);
        }
    }

    /**
     * dictValue:dictLabel
     */
    @Override
    public Map<String, String> dictDataIdMap(String dictType) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType);
        queryWrapper.eq(DictData::getIsEnable, Enable.ENABLE);
        return this.list(queryWrapper).stream().collect(Collectors.toMap(DictData::getDictValue, DictData::getDictLabel));
    }

    /**
     * 获取字典列表
     */
    @Override
    public List<DictLabelData> checkDictLabelList(String dictType, List<String> dictLabelList) {
        if (dictLabelList == null || dictLabelList.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType);
        queryWrapper.in(DictData::getDictLabel, dictLabelList);
        queryWrapper.eq(DictData::getIsEnable, Enable.ENABLE);
        List<DictData> dictDataList = this.list(queryWrapper);
        if (dictDataList == null || dictDataList.isEmpty()) {
            throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
        if (dictDataList.size() != dictLabelList.size()) {
            throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
        return dictDataList.stream().map(DictConvert.INSTANCE::toDictLabelData).collect(Collectors.toList());
    }

    /**
     * 校验字典列表
     */
    @Override
    public List<DictLabelData> checkDictValueList(String dictType, List<String> dictValueList) {
        if (dictValueList == null || dictValueList.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictData::getDictType, dictType);
        queryWrapper.in(DictData::getDictValue, dictValueList);
        queryWrapper.eq(DictData::getIsEnable, Enable.ENABLE);
        List<DictData> dictDataList = this.list(queryWrapper);
        if (dictDataList == null || dictDataList.isEmpty()) {
            throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
        if (dictDataList.size() != dictValueList.size()) {
            throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
        return dictDataList.stream().map(DictConvert.INSTANCE::toDictLabelData).collect(Collectors.toList());
    }
}
