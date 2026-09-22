package com.study.api.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class DeptPathData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID路径
     */
    private String deptPath;

    /**
     * 部门名称路径
     */
    private String deptPathName;
}