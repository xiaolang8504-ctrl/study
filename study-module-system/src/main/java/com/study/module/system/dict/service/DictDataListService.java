package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictDataPageListReq;
import com.study.module.system.dict.dto.response.DictDataListResp;
import com.study.module.system.dict.entity.DictData;
import com.study.common.core.domain.dto.PageResult;

/**
 * 字典列表服务
 */
public interface DictDataListService extends IService<DictData> {

    /**
     * 字典分页列表
     */
    PageResult<DictDataListResp> dictDataPageList(DictDataPageListReq request);
}
