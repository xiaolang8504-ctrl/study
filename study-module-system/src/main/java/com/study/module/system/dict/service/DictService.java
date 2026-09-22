package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictCreateReq;
import com.study.module.system.dict.entity.Dict;

/**
 * 字典服务
 */
public interface DictService extends IService<Dict> {

    /**
     * 检验字典
     */
    Dict checkDict(Integer id);

    /**
     * 检验字典类型
     */
    Dict checkDictType(String dictType);

    /**
     * 构建字典信息入库
     */
    Dict buildDict(DictCreateReq request);

    /**
     * 校验字典是否存在
     */
    void existDict(String dictType);

    /**
     * 校验字典是否存在
     */
    void existDict(Integer id, String dictType);

    /**
     * 根据字典类型获取字典
     */
    Dict dictTypeData(String dictType);
}
