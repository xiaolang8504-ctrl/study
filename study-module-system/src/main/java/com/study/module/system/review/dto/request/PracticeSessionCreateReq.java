package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建专项练习请求
 */
@Data
@ApiModel("创建专项练习请求")
public class PracticeSessionCreateReq {

    @ApiModelProperty(value = "练习类型：KNOWLEDGE知识点，ERROR_LABEL错因，TYPICAL典型错题", required = true)
    @NotBlank(message = "练习类型不能为空")
    private String practiceType;

    @ApiModelProperty("题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库，MIXED混合")
    private String questionSource;

    @ApiModelProperty("科目")
    private String subject;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("错因标签")
    private String errorLabel;

    @ApiModelProperty("难度")
    private Integer difficulty;

    @ApiModelProperty("是否包含已掌握错题，默认否")
    private Boolean includeMastered = false;

    @ApiModelProperty(value = "题目数量", required = true)
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量不能小于1")
    @Max(value = 50, message = "题目数量不能超过50")
    private Integer questionCount;
}
