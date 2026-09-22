package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictDataSortReq;
import com.study.module.system.dict.entity.DictData;

/**
 * 字典排序服务
 */
public interface DictDataSortService extends IService<DictData> {

    /**
     * 字典排序
     */
    void dictDataSort(DictDataSortReq request);
}
