package com.study.module.system.user.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 滑块验证码校验请求。
 */
@Data
public class SliderCaptchaVerifyReq {

    @NotBlank(message = "验证失败,请重新向右拖动滑块填充拼图")
    private String sliderCaptchaToken;

    @NotBlank(message = "验证失败,请重新向右拖动滑块填充拼图")
    private String sliderCaptchaOffset;
}
