package com.study.module.system.user.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.List;

@Data
public class UserPageListResp {

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
     * 状态（0停用 1正常）
     */
    private Integer status;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 部门名
     */
    private String deptName;

    /**
     * 部门ID路径
     */
    private String deptPath;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色名集
     */
    private String roleNames;

    /**
     * 角色ID集
     */
    private List<Integer> roleIds;

}
