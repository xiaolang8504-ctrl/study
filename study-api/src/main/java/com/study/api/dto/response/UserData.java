package com.study.api.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserData implements Serializable {

    private static final long serialVersionUID = 7955916170675189261L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 当前登录用户部门ID
     */
    private Integer deptId;

    /**
     * 当前登录用户部门名
     */
    private String deptName;

}
