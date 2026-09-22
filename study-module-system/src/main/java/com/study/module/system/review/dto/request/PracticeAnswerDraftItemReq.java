package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 专项练习草稿题目请求
 */
@Data
public class PracticeAnswerDraftItemReq {

    @ApiModelProperty(value = "练习题目ID", required = true)
    @NotNull(message = "练习题目ID不能为空")
    private Long sessionQuestionId;

    @ApiModelProperty("学生答案")
    private String studentAnswer;

    @ApiModelProperty("作答用时，单位秒")
    @Min(value = 0, message = "作答用时不能小于0")
    @Max(value = 86400, message = "单题作答用时不能超过24小时")
    private Integer durationSeconds;
}
