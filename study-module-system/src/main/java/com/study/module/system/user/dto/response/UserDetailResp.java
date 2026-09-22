package com.study.module.system.user.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Set;

@Data
public class UserDetailResp {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色集
     */
    private Set<String> roles;

    /**
     * api资源集
     */
    private Set<String> resources;
}