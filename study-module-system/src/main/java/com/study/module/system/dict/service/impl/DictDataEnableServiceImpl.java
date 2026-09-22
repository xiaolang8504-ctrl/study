package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataEnableService;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictDataUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字典数据启用服务
 */
@Service
public class DictDataEnableServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataEnableService {

    @Autowired
    DictDataService dictDataService;

    @Autowired
    DictDataUpdateService dictDataUpdateService;

    /**
     * 启用字典数据
     */
    @Override
    public void enableDictData(Integer id) {
        // 校验字典数据
        DictData dictData = dictDataService.checkDictData(id);
        // 更新字典数据状态
        dictDataUpdateService.updateDictDataStatus(dictData);
    }
}
