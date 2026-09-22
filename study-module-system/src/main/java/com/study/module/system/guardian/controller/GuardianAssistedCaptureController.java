package com.study.module.system.guardian.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.guardian.dto.request.GuardianAssistedCaptureCreateReq;
import com.study.module.system.guardian.dto.request.GuardianRelationIdReq;
import com.study.module.system.guardian.dto.response.GuardianAssistedCaptureResp;
import com.study.module.system.guardian.service.GuardianAssistedCaptureService;
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

import java.util.List;

/** 家长受控代上传接口。 */
@Api(tags = "家长代上传")
@RestController
@RequestMapping("/api/guardianAssistedCapture")
public class GuardianAssistedCaptureController {
    @Autowired private GuardianAssistedCaptureService guardianAssistedCaptureService;

    @ApiOperation("创建待学生确认的代上传")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createGuardianAssistedCapture")
    public Result<Long> createGuardianAssistedCapture(@RequestBody @Validated GuardianAssistedCaptureCreateReq request) {
        return ResultUtils.success(guardianAssistedCaptureService.createGuardianAssistedCapture(request));
    }
    @ApiOperation("学生确认代上传并创建采集任务")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/confirmGuardianAssistedCapture")
    public Result<Long> confirmGuardianAssistedCapture(@RequestBody @Validated GuardianRelationIdReq request) {
        return ResultUtils.success(guardianAssistedCaptureService.confirmGuardianAssistedCapture(request.getRelationId()));
    }
    @ApiOperation("撤销待确认代上传")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/revokeGuardianAssistedCapture")
    public Result<Void> revokeGuardianAssistedCapture(@RequestBody @Validated GuardianRelationIdReq request) {
        guardianAssistedCaptureService.revokeGuardianAssistedCapture(request.getRelationId()); return ResultUtils.success();
    }
    @ApiOperation("当前账号可见的代上传任务")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/guardianAssistedCaptureList")
    public Result<List<GuardianAssistedCaptureResp>> guardianAssistedCaptureList() {
        return ResultUtils.success(guardianAssistedCaptureService.guardianAssistedCaptureList());
    }
}
