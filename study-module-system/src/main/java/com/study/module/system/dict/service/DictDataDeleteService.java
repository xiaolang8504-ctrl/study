package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.entity.DictData;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典删除服务
 */
public interface DictDataDeleteService extends IService<DictData> {

    /**
     * 删除字典
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteDictData(Integer id);
}
