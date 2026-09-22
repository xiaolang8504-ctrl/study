package com.study.api.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class DeptData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Integer id;

    /**
     * 名称
     */
    private String deptName;

    /**
     * 部门路径IDS(逗号隔开)
     */
    private String deptPath;

    /**
     * 部门父ID
     */
    private Integer pid;
}