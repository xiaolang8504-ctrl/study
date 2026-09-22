package com.study.module.system.homework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.homework.dto.request.UpdateHomeWorkReq;
import com.study.module.system.homework.entity.HomeWork;

/**
 * 作业修改服务
 */
public interface HomeWorkUpdateService extends IService<HomeWork> {

    /**
     * 修改作业
     */
    void updateHomeWork(UpdateHomeWorkReq request);
}
