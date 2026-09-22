package com.study.module.system.resource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.resource.convert.ResourceConvert;
import com.study.module.system.resource.dto.request.CreateResourceReq;
import com.study.module.system.resource.dto.response.ResourceIdResp;
import com.study.module.system.resource.entity.Resource;
import com.study.module.system.resource.mapper.ResourceMapper;
import com.study.module.system.resource.service.ResourceUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.resource.service.ResourceCreateService;
import com.study.module.system.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 系统资源表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class ResourceCreateServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceCreateService {

    @Autowired
    ResourceService resourceService;

    @Autowired
    ResourceUpdateService resourceUpdateService;

    /**
     * 添加资源
     */
    @Override
    public ResourceIdResp createResource(CreateResourceReq request) {
        // 参数逻辑校验
        createValidate(request);

        //数据入库
        Integer id = createResourcetData(request);

        //返回新增ID值
        ResourceIdResp idResp = new ResourceIdResp();
        idResp.setId(id);
        return idResp;
    }

    /**
     * 参数逻辑校验
     */
    private void createValidate(CreateResourceReq request){
        if (resourceService.checkResourceCode(request.getCode())){
            throw new LogicException(ErrorCodeConstants.RES_CODE_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private Integer createResourcetData(CreateResourceReq request){
        Resource resource = ResourceConvert.INSTANCE.toResource(request);
        resource.setCreateTime(LocalDateTime.now());
        if (!this.save(resource)){
            throw new LogicException(ErrorCodeConstants.CREATE_RES_FAIL);
        }
        //更新sort值
        resourceUpdateService.updateResourceSort(resource.getId());
        return resource.getId();
    }
}