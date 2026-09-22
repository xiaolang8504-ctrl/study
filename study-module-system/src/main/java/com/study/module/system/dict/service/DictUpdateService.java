package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictUpdateReq;
import com.study.module.system.dict.entity.Dict;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典服务
 */
public interface DictUpdateService extends IService<Dict> {

    /**
     * 更新字典
     */
    @Transactional(rollbackFor = Exception.class)
    void updateDict(DictUpdateReq request);
}
