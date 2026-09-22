package com.study.module.system.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.constants.Delete;
import com.study.module.system.resource.dto.response.ResourceListResp;
import com.study.module.system.resource.entity.Resource;
import com.study.module.system.resource.convert.ResourceConvert;
import com.study.module.system.resource.mapper.ResourceMapper;
import com.study.module.system.resource.service.ResourceListService;
import com.study.module.system.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统资源表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class ResourceListServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceListService {

    @Autowired
    ResourceService resourceService;

    /**
     * 资源列表带父ID筛选
     */
    @Override
    public List<ResourceListResp> resourceList(Integer pid) {
        List<ResourceListResp> listRes = new ArrayList<>();
        this.list(new LambdaQueryWrapper<Resource>().eq(Resource::getPid,pid).orderByAsc(Resource::getSort)).stream().map(resource->{
            ResourceListResp resourceListResp = ResourceConvert.INSTANCE.toResourceList(resource);
            //是否有子级
            resourceListResp.setIsSon(resourceService.checkSonByPid(resource.getId()) ? Delete.YES : Delete.NO);
            listRes.add(resourceListResp);
            return resource;
        }).collect(Collectors.toList());
        return listRes;
    }

    /**
     * 获取所有的资源code
     */
    @Override
    public Set<String> resourceAllCode() {
        return this.list().stream().map(Resource::getCode).collect(Collectors.toSet());
    }
}