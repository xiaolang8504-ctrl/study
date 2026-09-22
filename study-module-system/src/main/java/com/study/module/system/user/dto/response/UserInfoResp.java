package com.study.module.system.user.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Set;

@Data
public class UserInfoResp {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账户
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 部门名
     */
    private String deptName;

    /**
     * 部门路径
     */
    private String deptPath;

    /**
     * 角色集
     */
    private Set<String> roles;

    /**
     * 角色名称集
     */
    private Set<String> roleNames;

    /**
     * API资源集
     */
    private Set<String> resources;

    /**
     * 菜单集
     */
    private Set<String> menu;

}