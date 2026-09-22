package com.study.module.system.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.dict.dto.request.DictPageListReq;
import com.study.module.system.dict.dto.response.DictListResp;
import com.study.module.system.dict.entity.Dict;
import com.study.common.core.domain.dto.PageResult;

/**
 * 字典服务
 */
public interface DictListService extends IService<Dict> {

    /**
     * 字典分页列表
     */
    PageResult<DictListResp> dictList(DictPageListReq request);
}
