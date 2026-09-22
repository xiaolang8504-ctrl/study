package com.study.module.system.user.dto.request;

import com.study.common.core.validation.IdsFormat;
import com.study.common.core.validation.IdsSize;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserReq {

    /**
     * 账号
     */
    @NotBlank(message ="账号不能为空")
    @Length(max = 30, message = "账号最长为{max}位")
    private String userName;

    /**
     * 手机号
     */
    @Length(max = 15, message = "手机号最长为{max}位")
    private String phone;

    /**
     * 真实姓名
     */
    @NotBlank(message ="姓名不能为空")
    @Length(max = 100, message = "真实姓名最长为{max}位")
    private String realName;

    /**
     * 邮箱
     */
    @NotBlank(message ="邮箱不能为空")
    @Length(max = 100, message = "邮箱最长为{max}位")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 备注
     */
    @Length(max = 50, message = "备注最长为{max}位")
    private String remark;

    /**
     * 部门ID
     */
    @NotNull(message ="部门不能为空")
    private Integer deptId;

    /**
     * 角色IDS
     */
    @IdsFormat(message = "角色IDS格式错误" , distinct = false)
    @IdsSize(max = 10, message = "角色数量不能超过{max}")
    @NotBlank(message ="角色不能为空")
    private String roleIds;
}