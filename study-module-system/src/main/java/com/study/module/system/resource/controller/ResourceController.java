package com.study.module.system.resource.controller;

import com.study.module.system.resource.service.*;
import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.resource.dto.request.CreateResourceReq;
import com.study.module.system.resource.dto.request.ResourceIdReq;
import com.study.module.system.resource.dto.request.UpdateResourceReq;
import com.study.module.system.resource.dto.response.ResourceIdResp;
import com.study.module.system.resource.dto.response.ResourceListResp;
import com.study.module.system.resource.dto.response.ResourceTreeResp;
import com.study.module.system.resource.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 资源前端控制器
 */
@SuppressWarnings({"Duplicates","unchecked"})
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    ResourceCreateService resourceCreateService;

    @Autowired
    ResourceDeleteService resourceDeleteService;

    @Autowired
    ResourceListService resourceListService;

    @Autowired
    ResourceUpdateService resourceUpdateService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    ResourceTreeService resourceTreeService;

    /**
     * 资源列表带父ID筛选
     */
    @PreAuthorize("hasAuthority('system:resource:resourceList')")
    @GetMapping("/resourceList")
    public Result<List<ResourceListResp>> resourceList(@RequestParam(defaultValue = "0") Integer pid) {
        return ResultUtils.success(resourceListService.resourceList(pid));
    }

    /**
     * API资源树形结构
     */
    @PreAuthorize("hasAuthority('system:resource:resourceTreeData')")
    @GetMapping("/resourceTreeData")
    public Result<List<ResourceTreeResp>> resourceTreeData() {
        return ResultUtils.success(resourceTreeService.resourceTreeData());
    }

    /**
     * 添加资源
     */
    @PreAuthorize("hasAuthority('system:resource:createResource')")
    @PostMapping("/createResource")
    public Result<ResourceIdResp> createResource(@RequestBody @Validated CreateResourceReq request) {
        return ResultUtils.success(resourceCreateService.createResource(request));
    }

    /**
     * 根据权限注解生成资源(权限注解入库)
     */
    @PreAuthorize("hasAuthority('system:resource:createPre')")
    @PostMapping("/createPre")
    public Result<List<Resource>> createPre() {
        return ResultUtils.success(resourceService.createPre());
    }

    /**
     * 更新资源
     */
    @PreAuthorize("hasAuthority('system:resource:updateResource')")
    @PostMapping("/updateResource")
    public Result<Void> updateResource(@RequestBody @Validated UpdateResourceReq request) {
        resourceUpdateService.updateResource(request);
        return ResultUtils.success();
    }

    /**
     * 删除资源
     */
    @PreAuthorize("hasAuthority('system:resource:deleteResource')")
    @PostMapping("/deleteResource")
    public Result<Void> deleteResource(@RequestBody @Validated ResourceIdReq request) {
        resourceDeleteService.deleteResource(request.getId());
        return ResultUtils.success();
    }

}
