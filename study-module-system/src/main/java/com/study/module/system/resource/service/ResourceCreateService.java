package com.study.module.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.resource.dto.request.CreateResourceReq;
import com.study.module.system.resource.dto.response.ResourceIdResp;
import com.study.module.system.resource.entity.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统资源表 服务类
 */
public interface ResourceCreateService extends IService<Resource> {

    /**
     * 添加资源
     */
    @Transactional(rollbackFor = Exception.class)
    ResourceIdResp createResource(CreateResourceReq request);
}