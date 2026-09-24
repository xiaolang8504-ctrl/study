package com.study.module.system.guardian.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.guardian.dto.request.GuardianWeeklyPlanSaveReq;
import com.study.module.system.guardian.dto.request.GuardianWeeklyPlanTodoReq;
import com.study.module.system.guardian.dto.response.GuardianWeeklyPlanResp;
import com.study.module.system.guardian.service.GuardianWeeklyPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 家长周计划与学生待办确认接口。 */
@Api(tags = "家长周计划")
@RestController
@RequestMapping("/api/guardianWeeklyPlan")
public class GuardianWeeklyPlanController {

    @Autowired
    GuardianWeeklyPlanService guardianWeeklyPlanService;

    @ApiOperation("保存学生独立周计划")
    @PreAuthorize("hasAuthority('system:guardian:saveGuardianWeeklyPlan')")
    @PostMapping("/saveGuardianWeeklyPlan")
    public Result<Void> saveGuardianWeeklyPlan(@RequestBody @Validated GuardianWeeklyPlanSaveReq request) {
        guardianWeeklyPlanService.saveGuardianWeeklyPlan(request);
        return ResultUtils.success();
    }

    @ApiOperation("请求学生确认周计划待办")
    @PreAuthorize("hasAuthority('system:guardian:requestGuardianWeeklyPlanTodo')")
    @PostMapping("/requestGuardianWeeklyPlanTodo")
    public Result<Void> requestGuardianWeeklyPlanTodo(@RequestBody @Validated GuardianWeeklyPlanTodoReq request) {
        guardianWeeklyPlanService.requestGuardianWeeklyPlanTodo(request.getPlanId());
        return ResultUtils.success();
    }

    @ApiOperation("学生确认或暂不接受周计划待办")
    @PreAuthorize("hasAuthority('system:guardian:confirmGuardianWeeklyPlanTodo')")
    @PostMapping("/confirmGuardianWeeklyPlanTodo")
    public Result<Void> confirmGuardianWeeklyPlanTodo(@RequestBody @Validated GuardianWeeklyPlanTodoReq request) {
        if (request.getConfirmed() == null) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_WEEKLY_PLAN_TODO_STATUS_INVALID);
        }
        guardianWeeklyPlanService.confirmGuardianWeeklyPlanTodo(request.getPlanId(), request.getConfirmed());
        return ResultUtils.success();
    }

    @ApiOperation("当前账号可见的家长周计划")
    @PreAuthorize("hasAuthority('system:guardian:guardianWeeklyPlanList')")
    @GetMapping("/guardianWeeklyPlanList")
    public Result<List<GuardianWeeklyPlanResp>> guardianWeeklyPlanList(@RequestParam(required = false) Long studentUserId) {
        return ResultUtils.success(guardianWeeklyPlanService.guardianWeeklyPlanList(studentUserId));
    }
}
