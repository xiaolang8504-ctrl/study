package com.study.module.system.user.dto.request;

import lombok.Data;

@Data
public class UserListReq {

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 用户ID集(多个逗号隔开)
     */
    private String ids;

    /**
     * 角色ID集(多个逗号隔开)
     */
    private String roleIds;
}