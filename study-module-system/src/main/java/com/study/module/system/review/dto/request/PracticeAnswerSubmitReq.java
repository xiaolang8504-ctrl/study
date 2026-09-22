package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 提交专项练习作答请求
 */
@Data
@ApiModel("提交专项练习作答请求")
public class PracticeAnswerSubmitReq {

    @ApiModelProperty(value = "练习题目ID", required = true)
    @NotNull(message = "练习题目ID不能为空")
    private Long sessionQuestionId;

    @ApiModelProperty(value = "学生答案", required = true)
    @NotBlank(message = "请先输入答案")
    private String studentAnswer;

    @ApiModelProperty("主观题自评结果: 0错误, 1正确")
    @Min(value = 0, message = "自评结果只能为0或1")
    @Max(value = 1, message = "自评结果只能为0或1")
    private Integer selfCorrect;

    @ApiModelProperty("作答用时，单位秒")
    @Min(value = 0, message = "作答用时不能小于0")
    @Max(value = 86400, message = "单题作答用时不能超过24小时")
    private Integer durationSeconds;
}
