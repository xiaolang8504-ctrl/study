package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.entity.Dict;

/**
 * 字典服务
 */
public interface DictDeleteService extends IService<Dict> {

    /**
     * 删除字典
     */
    void deleteDict(Integer id);
}
