package com.study.module.system.role.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateRoleMenuReq extends RoleIdReq {

    /**
     * 菜单Id集
     */
    @NotBlank(message ="角色对应的菜单不能为空")
    private String menuIds;

    /**
     * 菜单层级
     */
    @NotBlank(message ="角色对应的菜单层级不能为空")
    private String menuLevel;
}