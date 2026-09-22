package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.dto.request.DictCreateReq;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.mapper.DictMapper;
import com.study.module.system.dict.service.DictCreateService;
import com.study.module.system.dict.service.DictService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 字典服务
 */
@Service
public class DictCreateServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictCreateService {

    @Autowired
    DictService dictService;

    /**
     * 创建字典
     */
    @Override
    public void createDict(DictCreateReq request) {
        // 校验字典唯一
        dictService.existDict(request.getDictType());
        // 构建字典数据
        Dict dict = dictService.buildDict(request);
        // 额外信息
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());
        // 创建字典
        if (!this.save(dict)) {
            throw new LogicException(ErrorCodeConstants.DICT_CREATE_FAIL);
        }
    }
}
