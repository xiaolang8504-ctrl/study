package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictDataCreateReq;
import com.study.module.system.dict.entity.DictData;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典创建服务
 */
public interface DictDataCreateService extends IService<DictData> {

    /**
     * 创建字典
     */
    @Transactional(rollbackFor = Exception.class)
    void createDictData(DictDataCreateReq request);
}
