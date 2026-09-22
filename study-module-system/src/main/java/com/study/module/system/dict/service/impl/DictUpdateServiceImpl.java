package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.dto.request.DictUpdateReq;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.mapper.DictMapper;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictDataUpdateService;
import com.study.module.system.dict.service.DictService;
import com.study.module.system.dict.service.DictUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 字典服务
 */
@Service
public class DictUpdateServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictUpdateService {

    @Autowired
    DictService dictService;

    @Autowired
    DictDataUpdateService dictDataUpdateService;

    @Autowired
    DictDataService dictDataService;

    /**
     * 更新字典
     */
    @Override
    public void updateDict(DictUpdateReq request) {
        // 校验字典
        Dict oldDict = dictService.checkDict(request.getId());
        // 校验字典唯一
        dictService.existDict(request.getId(), request.getDictType());
        // 构建字典
        Dict dict = dictService.buildDict(request);
        // 补充额外信息
        dict.setId(request.getId());
        dict.setUpdateTime(LocalDateTime.now());
        // 更新字典
        if (!this.updateById(dict)) {
            throw new LogicException(ErrorCodeConstants.DICT_UPDATE_FAIL);
        }
        if (dictDataService.isExistDictData(oldDict.getDictType())) {
            // 更新字典类型
            dictDataUpdateService.updateDictType(oldDict.getDictType(), request.getDictType());
        }
    }
}
