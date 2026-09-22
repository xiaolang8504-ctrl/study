package com.study.module.system.homework.controller;

import com.study.module.system.homework.dto.request.HomeWorkIdReq;
import com.study.module.system.homework.dto.request.HomeWorkPageListReq;
import com.study.module.system.homework.dto.request.CreateHomeWorkReq;
import com.study.module.system.homework.dto.request.UpdateHomeWorkReq;
import com.study.module.system.homework.dto.response.HomeWorkDetailResp;
import com.study.module.system.homework.dto.response.HomeWorkPageListResp;
import com.study.module.system.homework.service.HomeWorkCreateService;
import com.study.module.system.homework.service.HomeWorkDeleteService;
import com.study.module.system.homework.service.HomeWorkDetailService;
import com.study.module.system.homework.service.HomeWorkListService;
import com.study.module.system.homework.service.HomeWorkUpdateService;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作业管理前端控制器
 */
@Api(tags = "作业管理")
@RestController
@RequestMapping("/api/homeWork")
public class HomeWorkController {

    @Autowired
    HomeWorkListService homeWorkListService;

    @Autowired
    HomeWorkDetailService homeWorkDetailService;

    @Autowired
    HomeWorkCreateService homeWorkCreateService;

    @Autowired
    HomeWorkUpdateService homeWorkUpdateService;

    @Autowired
    HomeWorkDeleteService homeWorkDeleteService;

    @ApiOperation("作业分页列表")
    // @PreAuthorize("hasAuthority('system:homeWork:homeWorkPageList')")
    /**
     * 分页查询作业。
     */
    @GetMapping("/homeWorkPageList")
    public Result<PageResult<HomeWorkPageListResp>> homeWorkPageList(@Validated HomeWorkPageListReq request) {
        return ResultUtils.success(homeWorkListService.homeWorkPageList(request));
    }

    @ApiOperation("作业详情")
    // @PreAuthorize("hasAuthority('system:homeWork:homeWorkDetail')")
    /**
     * 查询作业详情。
     */
    @GetMapping("/homeWorkDetail")
    public Result<HomeWorkDetailResp> homeWorkDetail(@Validated HomeWorkIdReq request) {
        return ResultUtils.success(homeWorkDetailService.homeWorkDetail(request.getId()));
    }

    @ApiOperation("新增作业")
    // @PreAuthorize("hasAuthority('system:homeWork:createHomeWork')")
    /**
     * 创建或保存作业。
     */
    @PostMapping("/createHomeWork")
    public Result<Void> createHomeWork(@RequestBody @Validated CreateHomeWorkReq request) {
        homeWorkCreateService.createHomeWork(request);
        return ResultUtils.success();
    }

    @ApiOperation("修改作业")
    // @PreAuthorize("hasAuthority('system:homeWork:updateHomeWork')")
    /**
     * 更新作业。
     */
    @PostMapping("/updateHomeWork")
    public Result<Void> updateHomeWork(@RequestBody @Validated UpdateHomeWorkReq request) {
        homeWorkUpdateService.updateHomeWork(request);
        return ResultUtils.success();
    }

    @ApiOperation("删除作业")
    // @PreAuthorize("hasAuthority('system:homeWork:deleteHomeWork')")
    /**
     * 删除作业。
     */
    @PostMapping("/deleteHomeWork")
    public Result<Void> deleteHomeWork(@RequestBody @Validated HomeWorkIdReq request) {
        homeWorkDeleteService.deleteHomeWork(request.getId());
        return ResultUtils.success();
    }
}
