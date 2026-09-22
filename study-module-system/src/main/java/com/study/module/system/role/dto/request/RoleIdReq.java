package com.study.module.system.role.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class RoleIdReq {

    /**
     * 角色ID
     */
    @NotNull(message ="角色ID不能为空")
    private Integer id;
}