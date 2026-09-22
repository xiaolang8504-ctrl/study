package com.study.api.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class RoleData implements Serializable {

    private static final long serialVersionUID = 7955916170675189261L;

    /**
     * 角色ID
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;
}