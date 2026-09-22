package com.study.module.system.guardian.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** 一次性家长邀请码响应。 */
@Data
public class GuardianInviteResp {

    @ApiModelProperty("监护关系ID")
    private Long relationId;
    @ApiModelProperty("仅本次创建时返回的邀请码，不会再次明文保存")
    private String invitationCode;
    @ApiModelProperty("邀请码过期时间")
    private LocalDateTime expireTime;
    @ApiModelProperty("当前绑定状态")
    private Integer status;
}
