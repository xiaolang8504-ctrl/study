package com.study.module.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.resource.dto.request.UpdateResourceReq;
import com.study.module.system.resource.entity.Resource;

/**
 * 系统资源表 服务类
 */
public interface ResourceUpdateService extends IService<Resource> {

    /**
     * 修改资源
     */
    void updateResource(UpdateResourceReq request);

    /**
     * 更新排序字段值
     */
    void updateResourceSort(Integer id);
}