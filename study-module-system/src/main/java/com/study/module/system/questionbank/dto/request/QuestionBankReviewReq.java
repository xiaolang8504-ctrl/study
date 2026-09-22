package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 题库题目审核请求
 */
@Data
public class QuestionBankReviewReq {

    @ApiModelProperty("主键ID")
    @NotNull(message = "题库题目ID不能为空")
    private Long id;

    @ApiModelProperty("审核状态")
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态不正确")
    @Max(value = 2, message = "审核状态不正确")
    private Integer reviewStatus;

    @ApiModelProperty("审核备注")
    private String reviewRemark;
}
