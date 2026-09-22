package com.study.module.system.dept.controller;

import com.study.module.system.dept.dto.request.CreateDeptReq;
import com.study.module.system.dept.dto.request.DeptIdReq;
import com.study.module.system.dept.dto.request.UpdateDeptReq;
import com.study.module.system.dept.dto.response.DeptDetailResp;
import com.study.module.system.dept.dto.response.DeptListResp;
import com.study.module.system.dept.dto.response.DeptTreeResp;
import com.study.module.system.dept.service.*;
import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织架构前端控制器
 */
@RestController
@RequestMapping("/api/dept")
public class DeptController {

    @Autowired
    DeptService deptService;

    @Autowired
    DeptListService deptListService;

    @Autowired
    DeptDetailService deptDetailService;

    @Autowired
    DeptTreeService deptTreeData;

    @Autowired
    DeptCreateService deptCreateService;

    @Autowired
    DeptUpdateService deptUpdateService;

    @Autowired
    DeptDeleteService deptDeleteService;

    @Autowired
    DeptEnableService deptEnableService;

    /**
     * 部门列表带父ID筛选
     */
    //@PreAuthorize("hasAuthority('system:dept:deptList')")
    @GetMapping("/deptList")
    public Result<List<DeptListResp>> deptList(@RequestParam(defaultValue = "0") Integer id) {
        return ResultUtils.success(deptListService.deptListById(id));
    }

    /**
     * 添加部门
     */
    //@PreAuthorize("hasAuthority('system:dept:createDept')")
    @PostMapping("/createDept")
    public Result<Void> createDept(@RequestBody @Validated CreateDeptReq request) {
        deptCreateService.createDept(request);
        return ResultUtils.success();
    }

    /**
     * 更新部门
     */
    //@PreAuthorize("hasAuthority('system:dept:updateDept')")
    @PostMapping("/updateDept")
    public Result<Void> updateDept(@RequestBody @Validated UpdateDeptReq request) {
        deptUpdateService.updateDept(request);
        return ResultUtils.success();
    }

    /**
     * 删除部门
     */
    //@PreAuthorize("hasAuthority('system:dept:deleteDept')")
    @PostMapping("/deleteDept")
    public Result<Void> deleteDept(@RequestBody @Validated DeptIdReq request) {
        deptDeleteService.deleteDept(request.getId());
        return ResultUtils.success();
    }

    /**
     * 部门ID获取详情
     */
    //@PreAuthorize("hasAuthority('system:dept:getDeptDetailById')")
    @GetMapping("/getDeptDetailById")
    public Result<DeptDetailResp> getDeptDetailById(@Validated DeptIdReq request) {
        return ResultUtils.success(deptDetailService.getDeptDetailById(request.getId()));
    }

    /**
     * 部门ID获取所有的子集的id集
     */
    //@PreAuthorize("hasAuthority('system:dept:getDeptSonIdsById')")
    @GetMapping("/getDeptSonIdsById")
    public Result<List<Integer>> getDeptSonIdsById(@Validated DeptIdReq request) {
        return ResultUtils.success(deptService.getDeptSonIdsById(request.getId()));
    }

    /**
     * 部门树形列表
     */
    //@PreAuthorize("hasAuthority('system:dept:deptTreeData')")
    @GetMapping("/deptTreeData")
    public Result<List<DeptTreeResp>> deptTreeData() {
        return ResultUtils.success(deptTreeData.deptTreeData());
    }

    /**
     * 启禁用部门
     */
    //@PreAuthorize("hasAuthority('system:dept:enableDept')")
    @PostMapping("/enableDept")
    public Result<Void> enableDept(@RequestBody @Validated DeptIdReq request) {
        deptEnableService.enableDept(request.getId());
        return ResultUtils.success();
    }
}
