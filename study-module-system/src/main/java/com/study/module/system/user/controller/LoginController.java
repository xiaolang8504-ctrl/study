package com.study.module.system.user.controller;

import com.study.module.system.user.dto.request.LoginForm;
import com.study.module.system.user.dto.request.SliderCaptchaVerifyReq;
import com.study.module.system.user.dto.response.LoginResp;
import com.study.module.system.user.service.LoginService;
import com.study.module.system.user.service.VerifyService;
import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    LoginService loginService;

    @Autowired
    VerifyService verifyService;

    /**
     * 校验登录滑块验证码。
     */
    @PostMapping(value = "/verifySliderCaptcha")
    public Result<Void> verifySliderCaptcha(@RequestBody @Validated SliderCaptchaVerifyReq request) {
        verifyService.verifySliderCaptcha(request);
        return ResultUtils.success();
    }

    /**
     * 用户登录
     */
    @PostMapping(value = "/login")
    public Result<LoginResp> login(@RequestBody @Validated LoginForm request) {
        return ResultUtils.success(loginService.login(request));
    }

    /**
     * 用户登录退出
     */
    @PostMapping(value = "/outLogin")
    public Result outLogin(HttpServletRequest request) {
        loginService.outLogin(request);
        return ResultUtils.success();
    }

}
