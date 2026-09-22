package com.study.module.system.user.dto.request;

import com.study.common.core.domain.dto.PageParam;
import lombok.Data;

@Data
public class UserPageListReq extends PageParam {

    /**
     * 账户
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
     * 状态（0停用 1正常）
     */
    private Integer status;

}