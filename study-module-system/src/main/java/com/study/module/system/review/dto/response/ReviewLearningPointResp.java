package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 知识点掌握统计响应类
 */
@Data
public class ReviewLearningPointResp {

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("复习题目数量")
    private Long questionCount;

    @ApiModelProperty("已掌握数量")
    private Long masteredCount;

    @ApiModelProperty("掌握率")
    private BigDecimal masteryRate;

    @ApiModelProperty("平均掌握度")
    private BigDecimal averageMasteryScore;
}
