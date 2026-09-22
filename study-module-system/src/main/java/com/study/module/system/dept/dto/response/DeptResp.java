package com.study.module.system.dept.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class DeptResp {

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
     * 部门ID路径
     */
    private String deptPath;

    /**
     * 部门名称路径
     */
    private String deptPathName;

    /**
     * 是否有子(1有,0无)
     */
    private Integer isSon;

    /**
     * 子
     */
    private List children;
}