package com.study.module.system.guardian.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 接受家长邀请码请求。 */
@Data
public class GuardianInviteAcceptReq {

    @ApiModelProperty(value = "学生提供的一次性邀请码", required = true)
    @NotBlank(message = "邀请码不能为空")
    @Size(max = 64, message = "邀请码格式不正确")
    private String invitationCode;
}
