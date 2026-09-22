package com.study.module.system.user.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户登录表单
 */
@Data
public class LoginForm {

    @Length(max = 30, message = "用户名最长为{max}位")
    @NotBlank(message ="用户名不能为空")
    private String userName;

    @Size(min = 6, max = 20, message = "密码长度为{min}-{max}之间")
    @NotBlank(message ="密码不能为空")
    private String passWord;

    @NotBlank(message ="请完成滑块验证")
    private String sliderCaptchaToken;
}
