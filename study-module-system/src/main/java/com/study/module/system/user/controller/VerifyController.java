package com.study.module.system.user.controller;

import com.study.module.system.user.dto.response.VerifyResp;
import com.study.module.system.user.service.VerifyService;
import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VerifyController {

    @Autowired
    VerifyService verifyService;

    /**
     * 获取图形验证码
     */
    @GetMapping("/verify")
    public Result<VerifyResp> verify() {
        return ResultUtils.success(verifyService.verify());
    }
}