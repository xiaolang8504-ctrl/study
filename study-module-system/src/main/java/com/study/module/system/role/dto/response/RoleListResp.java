package com.study.module.system.role.dto.response;

import lombok.Data;

@Data
public class RoleListResp {

    /**
     * 新增ID
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 菜单ID集合(id以逗号隔开)
     */
    private String menuIds;

    /**
     * 菜单层级id,分割：1-2-3,1-2-4
     */
    private String menuLevel;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 是否管理员(0否 1是)
     */
    private Integer isSystem;
}
