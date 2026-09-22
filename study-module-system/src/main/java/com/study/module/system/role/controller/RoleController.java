package com.study.module.system.role.controller;

import com.study.module.system.role.dto.request.*;
import com.study.module.system.role.dto.response.RoleDetailResp;
import com.study.module.system.role.dto.response.RoleListResp;
import com.study.module.system.role.service.*;
import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色前端控制器
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    RoleByMenuService roleByMenuService;

    @Autowired
    RoleCreateService roleCreateService;

    @Autowired
    RoleDeleteService roleDeleteService;

    @Autowired
    RoleDetailService roleDetailService;

    @Autowired
    RoleOptionsService roleOptionsService;

    @Autowired
    RoleUpdateService roleUpdateService;

    @Autowired
    RoleListService roleListService;

    /**
     * 角色列表带名称筛选
     */
    @PreAuthorize("hasAuthority('system:role:roleList')")
    @GetMapping("/roleList")
    public Result<List<RoleListResp>> roleList(@Validated RoleListReq request) {
        return ResultUtils.success(roleListService.roleList(request));
    }

    /**
     * 添加角色
     */
    @PreAuthorize("hasAuthority('system:role:createRole')")
    @PostMapping("/createRole")
    public Result<Void> createRole(@RequestBody @Validated CreateRoleReq request) {
        roleCreateService.createRole(request);
        return ResultUtils.success();
    }

    /**
     * 更新角色
     */
    @PreAuthorize("hasAuthority('system:role:updateRole')")
    @PostMapping("/updateRole")
    public Result<Void> updateRole(@RequestBody @Validated UpdateRoleReq request) {
        roleUpdateService.updateRole(request);
        return ResultUtils.success();
    }

    /**
     * 删除角色
     */
    @PreAuthorize("hasAuthority('system:role:deleteRole')")
    @PostMapping("/deleteRole")
    public Result<Void> deleteRole(@RequestBody @Validated RoleIdReq request) {
        roleDeleteService.deleteRole(request.getId());
        return ResultUtils.success();
    }

    /**
     * 角色详情
     */
    @PreAuthorize("hasAuthority('system:role:roleDetail')")
    @GetMapping("/roleDetail")
    public Result<RoleDetailResp> roleDetail(@Validated RoleDetailReq request) {
        return ResultUtils.success(roleDetailService.roleDetail(request));
    }

    /**
     * 角色下拉列表
     */
    @PreAuthorize("hasAuthority('system:role:roleOptions')")
    @GetMapping("/roleOptions")
    public Result<List<Map<String, Object>>> roleOptions() {
        return ResultUtils.success(roleOptionsService.roleOptions());
    }

    /**
     * 角色分配菜单
     */
    @PreAuthorize("hasAuthority('system:role:roleByMenu')")
    @PostMapping("/roleByMenu")
    public Result<Void> roleByMenu(@RequestBody @Validated UpdateRoleMenuReq request) {
        roleByMenuService.roleByMenu(request);
        return ResultUtils.success();
    }

    /**
     * 角色列表(审批)
     */
    @PreAuthorize("hasAuthority('account:role:roleListById')")
    @GetMapping("/roleListById")
    public Result roleListById(@Validated RoleIdSReq request) {
        return ResultUtils.success(roleListService.roleListById(request.getIds()));
    }
}
