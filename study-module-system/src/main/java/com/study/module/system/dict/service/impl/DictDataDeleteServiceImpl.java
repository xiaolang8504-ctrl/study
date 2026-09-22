package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataDeleteService;
import com.study.module.system.dict.service.DictDataService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字典数据删除服务
 */
@Service
public class DictDataDeleteServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataDeleteService {

    @Autowired
    DictDataService dictDataService;

    /**
     * 删除字典数据
     */
    @Override
    public void deleteDictData(Integer id) {
        // 校验字典数据
        dictDataService.checkDictData(id);
        // 删除字典数据
        if (!this.removeById(id)) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_DELETE_FAIL);
        }
    }
}
