package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.mapper.DictMapper;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictDeleteService;
import com.study.module.system.dict.service.DictService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字典服务
 */
@Service
public class DictDeleteServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictDeleteService {

    @Autowired
    DictService dictService;

    @Autowired
    DictDataService dictDataService;

    /**
     * 删除字典
     */
    @Override
    public void deleteDict(Integer id) {
        // 校验字典
        Dict dict = dictService.checkDict(id);
        // 校验字典数据
        if (dictDataService.isExistDictData(dict.getDictType())) {
            throw new LogicException(ErrorCodeConstants.EXIST_DICT_DATA);
        }
        // 删除字典
        if (!this.removeById(id)) {
            throw new LogicException(ErrorCodeConstants.DICT_DELETE_FAIL);
        }
    }
}
