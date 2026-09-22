package com.study.module.system.resource.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ResourceReq {

    /**
     * API资源名称
     */
    @NotBlank(message ="名称不能为空")
    @Length(max = 20, message = "名称最长为{max}位")
    private String resourceName;

    /**
     * API资源编码
     */
    @NotBlank(message ="编码不能为空")
    @Size(min = 3, max = 100, message = "编码长度为{min}-{max}之间")
    private String code;

    /**
     * API资源父Id
     */
    @NotNull(message ="父ID不能为空")
    @Range(min=0, max=1000, message = "父ID值范围为{min}-{max}之间")
    private Integer pid;
}