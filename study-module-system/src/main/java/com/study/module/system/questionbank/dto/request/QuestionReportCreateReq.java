package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 题目举报新增请求
 */
@Data
public class QuestionReportCreateReq {

    @ApiModelProperty("题库题目ID")
    @NotNull(message = "题库题目ID不能为空")
    private Long bankQuestionId;

    @ApiModelProperty("举报类型")
    @NotBlank(message = "举报类型不能为空")
    private String reportType;

    @ApiModelProperty("举报内容")
    private String reportContent;
}
