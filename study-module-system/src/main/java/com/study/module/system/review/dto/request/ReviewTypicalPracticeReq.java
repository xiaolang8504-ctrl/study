package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 典型错题练习请求
 */
@Data
public class ReviewTypicalPracticeReq {

    @ApiModelProperty("科目字典键值；为空表示全部科目")
    @Length(max = 50, message = "科目最长为{max}位")
    private String subject;

    @ApiModelProperty(value = "题目数量，默认5道", required = true)
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量不能少于{value}道")
    @Max(value = 20, message = "题目数量不能超过{value}道")
    private Integer questionCount;
}
