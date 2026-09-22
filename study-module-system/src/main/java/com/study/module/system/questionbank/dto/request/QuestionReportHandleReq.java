package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 题目举报处理请求
 */
@Data
public class QuestionReportHandleReq {

    @ApiModelProperty("题目举报ID")
    @NotNull(message = "题目举报ID不能为空")
    private Long id;

    @ApiModelProperty("处理状态")
    @NotNull(message = "处理状态不能为空")
    @Min(value = 1, message = "处理状态不正确")
    @Max(value = 2, message = "处理状态不正确")
    private Integer status;

    @ApiModelProperty("处理备注")
    private String handleRemark;

    /**
     * 处理为有效问题时可同步停用题目。
     */
    @ApiModelProperty("是否同步停用题目")
    private Boolean disableQuestion;
}
