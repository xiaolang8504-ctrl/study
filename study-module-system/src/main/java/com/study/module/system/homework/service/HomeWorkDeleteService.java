package com.study.module.system.homework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.homework.entity.HomeWork;

/**
 * 作业删除服务
 */
public interface HomeWorkDeleteService extends IService<HomeWork> {

    /**
     * 删除作业
     */
    void deleteHomeWork(Long id);
}
