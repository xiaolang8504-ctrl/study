package com.study.module.system.guardian.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.guardian.dto.response.GuardianStudentOverviewResp;
import com.study.module.system.guardian.service.GuardianOverviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 家长端只读学习概览接口。 */
@Api(tags = "家长学习概览")
@RestController
@RequestMapping("/api/guardianOverview")
public class GuardianOverviewController {

    @Autowired
    private GuardianOverviewService guardianOverviewService;

    @ApiOperation("查看已授权学生的学习概览")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/studentOverview")
    public Result<GuardianStudentOverviewResp> studentOverview(@RequestParam Long studentUserId) {
        return ResultUtils.success(guardianOverviewService.studentOverview(studentUserId));
    }

    @ApiOperation("导出已授权学生的学习汇总")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exportStudentOverview")
    public Result<GuardianStudentOverviewResp> exportStudentOverview(@RequestParam Long studentUserId) {
        return ResultUtils.success(guardianOverviewService.exportStudentOverview(studentUserId));
    }
}
