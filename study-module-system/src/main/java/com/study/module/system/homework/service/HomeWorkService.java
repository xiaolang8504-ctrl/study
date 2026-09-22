package com.study.module.system.homework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.homework.entity.HomeWork;

/**
 * 作业公共服务
 */
public interface HomeWorkService extends IService<HomeWork> {

    /**
     * 校验作业
     */
    HomeWork checkHomeWork(Long id);

    /**
     * 根据年级和科目字典键值填充名称
     */
    void fillDictNames(HomeWork homeWork);
}
