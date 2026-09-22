package com.study.module.system.resource.convert;

import com.study.module.system.resource.dto.request.CreateResourceReq;
import com.study.module.system.resource.dto.request.UpdateResourceReq;
import com.study.module.system.resource.dto.response.ResourceListResp;
import com.study.module.system.resource.dto.response.ResourceTreeResp;
import com.study.module.system.resource.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 系统资源 MapStruct 接口
 */
@Mapper
public interface ResourceConvert {

    ResourceConvert INSTANCE=Mappers.getMapper(ResourceConvert.class);

    /**
     * 资源列表数据处理
     */
    ResourceListResp toResourceList(Resource resource);

    /**
     * API资源树形结构数据处理
     */
    List<ResourceTreeResp> toResourceTreeResp(List<Resource> resources);

    /**
     * 资源入库数据处理
     */
    Resource toResource(CreateResourceReq createResourceReq);

    /**
     * 资源入库数据处理
     */
    Resource toResource(UpdateResourceReq updateResourceReq);
}
