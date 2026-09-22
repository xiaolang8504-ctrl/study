package com.study.module.system.role.dto.request;

import lombok.Data;

@Data
public class RoleListReq {

    /**
     * 角色ID
     */
    private Integer id;

    /**
     * 角色名
     */
    private String roleName;
}