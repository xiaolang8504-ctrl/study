package com.study.module.system.guardian.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.guardian.dto.request.GuardianInviteAcceptReq;
import com.study.module.system.guardian.dto.request.GuardianInviteCreateReq;
import com.study.module.system.guardian.dto.request.GuardianRelationIdReq;
import com.study.module.system.guardian.dto.response.GuardianBindingResp;
import com.study.module.system.guardian.dto.response.GuardianInviteResp;
import com.study.module.system.guardian.service.GuardianBindingService;
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

/**
 * 家长 PC Web 的受控绑定入口。
 * 所有接口均要求登录，服务层再按当前账号校验关系归属，避免仅依赖前端角色判断。
 */
@Api(tags = "家庭监护关系")
@RestController
@RequestMapping("/api/guardianBinding")
public class GuardianBindingController {

    @Autowired
    private GuardianBindingService guardianBindingService;

    @ApiOperation("学生创建家长邀请码")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createGuardianInvitation")
    public Result<GuardianInviteResp> createGuardianInvitation(
            @RequestBody @Validated GuardianInviteCreateReq request) {
        return ResultUtils.success(guardianBindingService.createGuardianInvitation(request));
    }

    @ApiOperation("家长接受邀请码")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/acceptGuardianInvitation")
    public Result<Void> acceptGuardianInvitation(@RequestBody @Validated GuardianInviteAcceptReq request) {
        guardianBindingService.acceptGuardianInvitation(request.getInvitationCode());
        return ResultUtils.success();
    }

    @ApiOperation("学生确认家长绑定")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/confirmGuardianBinding")
    public Result<Void> confirmGuardianBinding(@RequestBody @Validated GuardianRelationIdReq request) {
        guardianBindingService.confirmGuardianBinding(request.getRelationId());
        return ResultUtils.success();
    }

    @ApiOperation("学生或家长解绑")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/revokeGuardianBinding")
    public Result<Void> revokeGuardianBinding(@RequestBody @Validated GuardianRelationIdReq request) {
        guardianBindingService.revokeGuardianBinding(request.getRelationId());
        return ResultUtils.success();
    }

    @ApiOperation("当前账号可见的监护绑定列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/currentGuardianBindingList")
    public Result<List<GuardianBindingResp>> currentGuardianBindingList() {
        return ResultUtils.success(guardianBindingService.currentGuardianBindingList());
    }
}
