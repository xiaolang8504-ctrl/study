package com.study.module.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.resource.dto.response.ResourceTreeResp;
import com.study.module.system.resource.entity.Resource;

import java.util.List;

/**
 * API资源树形结构服务
 */
public interface ResourceTreeService extends IService<Resource> {

    /**
     * API资源树形结构
     */
    List<ResourceTreeResp> resourceTreeData();
}
