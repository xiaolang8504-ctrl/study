package com.study.module.system.guardian.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/** 创建家长邀请码请求。 */
@Data
public class GuardianInviteCreateReq {

    @ApiModelProperty(value = "关系，如 FATHER、MOTHER、GUARDIAN", example = "GUARDIAN")
    @Size(max = 32, message = "关系类型长度不能超过32个字符")
    private String relationType;
}
