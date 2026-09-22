package com.study.module.system.dept.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DeptReq {

    /**
     * 部门名称
     */
    @NotBlank(message ="部门名称不能为空")
    @Length(max = 50, message = "部门名称最长为{max}位")
    private String deptName;

    /**
     * 父ID
     */
    @NotNull(message ="父ID不能为空")
    @Range(min=0, max=1000, message = "父ID值范围为{min}-{max}之间")
    private Integer pid;
}