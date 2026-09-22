package com.study.module.system.user.dto.response;

import lombok.Data;

@Data
public class VerifyResp {

    /**
     * 滑块验证码背景图
     */
    private String background;

    /**
     * 可拖动的拼图块
     */
    private String sliderImage;

    /**
     * 滑块验证码令牌
     */
    private String sliderCaptchaToken;

}
