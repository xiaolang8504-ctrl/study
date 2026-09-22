package com.study.module.system.homework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.homework.dto.request.HomeWorkPageListReq;
import com.study.module.system.homework.dto.response.HomeWorkPageListResp;
import com.study.module.system.homework.entity.HomeWork;
import com.study.common.core.domain.dto.PageResult;

/**
 * 作业列表服务
 */
public interface HomeWorkListService extends IService<HomeWork> {

    /**
     * 分页查询作业
     */
    PageResult<HomeWorkPageListResp> homeWorkPageList(HomeWorkPageListReq request);
}
