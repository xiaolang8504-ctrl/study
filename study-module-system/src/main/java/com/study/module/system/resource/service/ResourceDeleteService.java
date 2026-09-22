package com.study.module.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.resource.entity.Resource;

/**
 * 系统资源表 服务类
 */
public interface ResourceDeleteService extends IService<Resource> {

    /**
     * 删除资源
     */
    void deleteResource(Integer id);
}