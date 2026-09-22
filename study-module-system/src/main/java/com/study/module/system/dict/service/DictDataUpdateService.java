package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictDataUpdateReq;
import com.study.module.system.dict.entity.DictData;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典更新服务
 */
public interface DictDataUpdateService extends IService<DictData> {

    /**
     * 更新字典
     */
    @Transactional(rollbackFor = Exception.class)
    void updateDictData(DictDataUpdateReq request);

    /**
     * 更新字典状态
     */
    void updateDictDataStatus(DictData dictData);

    /**
     * 更新字典类型
     */
    void updateDictType(String oldDictType, String newDictType);
}
