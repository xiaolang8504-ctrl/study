package com.study.module.system.user.service;

import com.study.module.system.user.dto.request.SliderCaptchaVerifyReq;
import com.study.module.system.user.dto.response.VerifyResp;

public interface VerifyService {

    /**
     * 获取图形验证码
     */
    VerifyResp verify();

    /**
     * 校验滑块验证码。
     */
    void verifySliderCaptcha(SliderCaptchaVerifyReq request);

    /**
     * 消费已通过校验的滑块验证码。
     */
    void consumeVerifiedSliderCaptcha(String sliderCaptchaToken);
}
