package com.study.module.system.dict.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.dto.request.DictDataSortReq;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictDataSortService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字典数据排序服务
 */
@Service
public class DictDataSortServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataSortService {

    @Autowired
    DictDataService dictDataService;

    /**
     * 字典数据排序
     */
    @Override
    public void dictDataSort(DictDataSortReq request) {
        // 校验初始位置字典数据ID
        DictData originDictData = dictDataService.checkDictData(request.getId());
        // 获取字典数据分页列表排序上下行ID(type: 0上移,1下移)
        Integer targetDictId = getAroundDictById(originDictData.getId(), request.getType());
        // 校验目标位置字典数据ID
        DictData targetDictData = dictDataService.checkDictData(targetDictId);
        boolean moveResult = this.update(updateDictSort(request.getId(), targetDictData))
                && this.update(updateDictSort(targetDictData.getId(), originDictData));
        if (!moveResult) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_SORT_FAIL);
        }
    }

    /**
     * 字典数据分页列表的上下条ID(type: 0上移, 1下移)
     */
    private Integer getAroundDictById(Integer id, Integer type) {
        DictData dictData = dictDataService.checkDictData(id);
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictData.getDictType()), DictData::getDictType, dictData.getDictType());
        if (type.equals(0)) {
            queryWrapper.gt(ObjectUtil.isNotEmpty(dictData.getDictDataSort()), DictData::getDictDataSort, dictData.getDictDataSort())
                    .orderByAsc(DictData::getDictDataSort)
                    .last("limit 1");
        } else {
            queryWrapper.lt(ObjectUtil.isNotEmpty(dictData.getDictDataSort()), DictData::getDictDataSort, dictData.getDictDataSort())
                    .orderByDesc(DictData::getDictDataSort)
                    .last("limit 1");
        }
        DictData dictDataAround = this.getOne(queryWrapper);
        if (dictDataAround == null) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_NOT_MOVE);
        }
        return dictDataAround.getId();
    }

    /**
     * 更新排序
     */
    private LambdaUpdateWrapper<DictData> updateDictSort(Integer id, DictData dictData) {
        LambdaUpdateWrapper<DictData> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DictData::getId, id);
        updateWrapper.set(DictData::getDictDataSort, dictData.getDictDataSort());
        return updateWrapper;
    }
}
