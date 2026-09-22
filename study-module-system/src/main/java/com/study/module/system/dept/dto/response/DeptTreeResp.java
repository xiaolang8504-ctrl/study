package com.study.module.system.dept.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class DeptTreeResp {

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
     * 子
     */
    private List children;
}