package com.study.module.system.homework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.homework.dto.request.CreateHomeWorkReq;
import com.study.module.system.homework.entity.HomeWork;

/**
 * 作业新增服务
 */
public interface HomeWorkCreateService extends IService<HomeWork> {

    /**
     * 新增作业
     */
    void createHomeWork(CreateHomeWorkReq request);
}
