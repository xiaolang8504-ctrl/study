package com.study.module.system.user.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserUnionRoleReq {

    /**
     * 用户ID
     */
    @NotNull(message ="用户ID不能为空")
    private Long id;

    /**
     * 角色IDS集(逗号隔开)
     */
    @NotBlank(message ="角色ID不能为空")
    private String roleIds;

}