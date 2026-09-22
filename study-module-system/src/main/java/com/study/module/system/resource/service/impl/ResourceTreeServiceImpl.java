package com.study.module.system.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.resource.convert.ResourceConvert;
import com.study.module.system.resource.dto.response.ResourceTreeResp;
import com.study.module.system.resource.entity.Resource;
import com.study.module.system.resource.mapper.ResourceMapper;
import com.study.module.system.resource.service.ResourceTreeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API资源树形结构服务实现
 */
@Service
public class ResourceTreeServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceTreeService {

    /**
     * 构建资源树
     */
    @Override
    public List<ResourceTreeResp> resourceTreeData() {
        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Resource::getId, Resource::getCode, Resource::getResourceName,
                Resource::getPid, Resource::getSort)
                .orderByAsc(Resource::getSort);
        List<ResourceTreeResp> resourceList = ResourceConvert.INSTANCE.toResourceTreeResp(this.list(queryWrapper));
        Map<Integer, List<ResourceTreeResp>> resourceMap = resourceList.stream()
                .collect(Collectors.groupingBy(ResourceTreeResp::getPid));
        return getChildren(resourceMap, 0);
    }

    /**
     * 递归构建子节点
     */
    private List<ResourceTreeResp> getChildren(Map<Integer, List<ResourceTreeResp>> resourceMap, Integer parentId) {
        List<ResourceTreeResp> resources = resourceMap.getOrDefault(parentId, new ArrayList<>());
        return resources.stream().peek(resource -> {
            resource.setChildren(getChildren(resourceMap, resource.getId()));
            resource.setIsSon(!resource.getChildren().isEmpty());
        }).collect(Collectors.toList());
    }
}
