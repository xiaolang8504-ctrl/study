package com.study.module.system.dept.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class DeptIdReq {

    /**
     * 部门ID
     */
    @NotNull(message ="部门ID不能为空")
    private Integer id;
}