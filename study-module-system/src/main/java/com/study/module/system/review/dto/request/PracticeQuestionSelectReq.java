package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 练习卷手工选题请求。
 */
@Data
@ApiModel("练习卷手工选题请求")
public class PracticeQuestionSelectReq {

    @ApiModelProperty(value = "题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库", required = true)
    @NotBlank(message = "题目来源不能为空")
    private String questionSource;

    @ApiModelProperty(value = "题目ID", required = true)
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
}
