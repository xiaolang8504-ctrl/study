package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.entity.Dict;
import com.study.common.core.domain.KeyValue;

import java.util.List;

/**
 * 字典下拉选项服务
 */
public interface DictOptionsService extends IService<Dict> {

    /**
     * 字典下拉选项
     */
    List<KeyValue<Long, String>> dictOptions();
}
