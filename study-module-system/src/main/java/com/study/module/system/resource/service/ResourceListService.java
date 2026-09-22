package com.study.module.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.resource.dto.response.ResourceListResp;
import com.study.module.system.resource.entity.Resource;

import java.util.List;
import java.util.Set;

/**
 * 系统资源表 服务类
 */
public interface ResourceListService extends IService<Resource> {

    /**
     * 资源列表带父ID筛选
     */
    List<ResourceListResp> resourceList(Integer pid);

    /**
     * 获取所有的资源code
     */
    Set<String> resourceAllCode();
}