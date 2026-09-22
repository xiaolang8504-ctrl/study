package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.entity.DictData;
import com.study.common.core.domain.KeyValue;

import java.util.List;

/**
 * 字典下拉选项服务
 */
public interface DictDataOptionsService extends IService<DictData> {

    /**
     * 字典下拉选项
     */
    List<KeyValue<String, String>> dictDataOptions(String dictType);
}
