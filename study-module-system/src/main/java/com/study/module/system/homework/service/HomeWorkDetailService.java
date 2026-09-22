package com.study.module.system.homework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.homework.dto.response.HomeWorkDetailResp;
import com.study.module.system.homework.entity.HomeWork;

/**
 * 作业详情服务
 */
public interface HomeWorkDetailService extends IService<HomeWork> {

    /**
     * 查询作业详情
     */
    HomeWorkDetailResp homeWorkDetail(Long id);
}
