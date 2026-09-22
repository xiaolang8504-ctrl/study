package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 薄弱知识点专项练习请求
 */
@Data
public class ReviewWeakPointPracticeReq {

    @ApiModelProperty(value = "知识点", required = true)
    @NotBlank(message = "知识点不能为空")
    @Length(max = 255, message = "知识点最长为{max}位")
    private String learningPoint;

    @ApiModelProperty(value = "题目数量，默认5道", required = true)
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量不能少于{value}道")
    @Max(value = 20, message = "题目数量不能超过{value}道")
    private Integer questionCount;
}
