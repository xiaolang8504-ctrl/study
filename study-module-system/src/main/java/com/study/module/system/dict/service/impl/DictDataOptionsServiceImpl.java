package com.study.module.system.dict.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataOptionsService;
import com.study.module.system.dict.service.DictService;
import com.study.common.core.constants.Enable;
import com.study.common.core.domain.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据下拉选项服务
 */
@Service
public class DictDataOptionsServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataOptionsService {

    @Autowired
    DictService dictService;

    /**
     * 字典数据下拉选项
     */
    @Override
    public List<KeyValue<String, String>> dictDataOptions(String dictType) {
        // 校验字典类型
        dictService.checkDictType(dictType);
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(dictType), DictData::getDictType, dictType);
        queryWrapper.eq(DictData::getIsEnable, Enable.ENABLE);
        queryWrapper.orderByDesc(DictData::getDictDataSort);
        return this.baseMapper.selectMaps(queryWrapper).stream().map(map -> {
            KeyValue<String, String> response = new KeyValue<>();
            response.setKey(String.valueOf(map.get("dictValue")));
            response.setValue(String.valueOf(map.get("dictLabel")));
            return response;
        }).collect(Collectors.toList());
    }
}
