package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.entity.DictData;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典启用服务
 */
public interface DictDataEnableService extends IService<DictData> {

    /**
     * 启用字典
     */
    @Transactional(rollbackFor = Exception.class)
    void enableDictData(Integer id);
}
