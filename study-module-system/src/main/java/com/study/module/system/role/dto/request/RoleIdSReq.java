package com.study.module.system.role.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RoleIdSReq {

    /**
     * 角色IDS
     */
    @NotBlank(message ="角色ID不能为空")
    private String ids;
}