package com.study.module.system.dict.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.dto.request.DictDataUpdateReq;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictDataUpdateService;
import com.study.module.system.dict.service.DictService;
import com.study.common.core.constants.Enable;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 字典数据更新服务
 */
@Service
public class DictDataUpdateServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataUpdateService {

    @Autowired
    DictService dictService;

    @Autowired
    DictDataService dictDataService;

    /**
     * 更新字典数据
     */
    @Override
    public void updateDictData(DictDataUpdateReq request) {
        // 校验字典数据数据
        dictDataService.checkDictData(request.getId());
        dictDataService.checkDictData(request.getId(), request.getDictType());
        // 校验字典
        dictService.checkDictType(request.getDictType());
        // 校验字典数据键值唯一
        dictDataService.existDictDataValue(request.getId(), request.getDictType(), request.getDictValue());
        // 校验字典数据名唯一
        dictDataService.existDictDataLabel(request.getId(), request.getDictType(), request.getDictValue());
        // 组装更新字典数据
        DictData dictData = dictDataService.buildDictData(request);
        dictData.setId(request.getId());
        dictData.setUpdateTime(LocalDateTime.now());
        // 更新字典数据
        if (!this.updateById(dictData)) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_UPDATE_FAIL);
        }
    }

    /**
     * 更新字典数据状态
     */
    public void updateDictDataStatus(DictData dictData) {
        LambdaUpdateWrapper<DictData> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Objects.nonNull(dictData.getId()), DictData::getId, dictData.getId());
        Integer isEnable = dictData.getIsEnable();
        updateWrapper.set(DictData::getIsEnable, isEnable == Enable.ENABLE ? Enable.DISABLE : Enable.ENABLE);
        boolean updateDictStatusResult = this.update(updateWrapper);
        if (!updateDictStatusResult) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_ENABLE_FAIL);
        }
    }

    /**
     * 更新字典类型
     */
    @Override
    public void updateDictType(String oldDictType, String newDictType) {
        if (ObjectUtil.isNotEmpty(oldDictType) && ObjectUtil.isNotEmpty(newDictType)) {
            if (oldDictType.equals(newDictType)) {
                return;
            }
            LambdaUpdateWrapper<DictData> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ObjectUtil.isNotEmpty(oldDictType), DictData::getDictType, oldDictType);
            updateWrapper.set(DictData::getDictType, newDictType);
            boolean updateDictStatusResult = this.update(updateWrapper);
            if (!updateDictStatusResult) {
                throw new LogicException(ErrorCodeConstants.UPDATE_DICT_TYPE_FAIL);
            }
        }
    }
}
