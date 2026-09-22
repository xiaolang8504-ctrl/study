package com.study.module.system.role.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

@Data
public class RoleReq {

    /**
     * 角色名称
     */
    @NotBlank(message ="名称不能为空")
    @Length(max = 50, message = "名称最长为{max}位")
    private String roleName;
}