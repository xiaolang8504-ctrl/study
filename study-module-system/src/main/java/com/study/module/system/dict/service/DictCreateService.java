package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictCreateReq;
import com.study.module.system.dict.entity.Dict;

/**
 * 字典服务
 */
public interface DictCreateService extends IService<Dict> {

    /**
     * 创建字典
     */
    void createDict(DictCreateReq request);
}
