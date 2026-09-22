package com.study.module.system.dept.dto.response;

import lombok.Data;

@Data
public class DeptDetailResp {

    /**
     * 部门ID
     */
    private Integer id;

    /**
     * 部门父ID
     */
    private Integer pid;

    /**
     * 部门名
     */
    private String deptName;

    /**
     * 路径
     */
    private String deptPath;
}