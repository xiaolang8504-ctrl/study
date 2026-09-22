package com.study.module.system.user.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 修改密码
 */
@Data
public class ChangeUserPwdForm {

    @NotBlank(message ="原密码不能为空")
    private String passWord;

    @Pattern(regexp="^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\\\W_.@$!%*#~?&^]+$)(?![a-z0-9]+$)(?![a-z\\\\W_.@$!%*#~?&^]+$)(?![0-9\\\\W_.@$!%*#~?&^]+$)[a-zA-Z0-9\\\\W_.@$!%*#~?&^]{8,20}$",message="需包含大小写字母、数字和特殊字符其中任意三种组合，长度要求8到20位")
    @NotBlank(message ="新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度为{min}-{max}之间")
    private String newPassWord;

    @NotBlank(message ="确认新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度为{min}-{max}之间")
    private String confirmPassWord;
}