package com.study.module.system.dept.dto.response;

import lombok.Data;

@Data
public class DeptListResp {

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
     * 父名称
     */
    private String parentName;

    /**
     * 是否启用: 0否, 1是
     */
    private Integer status;

    /**
     * 部门排序
     */
    private Integer sort;

    /**
     * 是否有子(1有,0无)
     */
    private Integer isSon;
}