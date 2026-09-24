package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 查询纸面练习卷回填详情请求。
 */
@Data
@ApiModel("查询纸面练习卷回填详情请求")
public class PracticePaperFillDetailReq {

    @ApiModelProperty(value = "纸面短码，如P0000000012", required = true)
    @NotBlank(message = "纸面短码不能为空")
    @Size(max = 32, message = "纸面短码长度不能超过32个字符")
    private String paperCode;
}
