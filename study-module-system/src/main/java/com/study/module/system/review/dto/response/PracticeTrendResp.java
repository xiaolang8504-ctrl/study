package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专项练习趋势响应
 */
@Data
public class PracticeTrendResp {

    @ApiModelProperty("周期天数")
    private Integer periodDays;

    @ApiModelProperty("练习次数")
    private Integer sessionCount;

    @ApiModelProperty("已答数量")
    private Integer answeredCount;

    @ApiModelProperty("正确数量")
    private Integer correctCount;

    @ApiModelProperty("错误数量")
    private Integer wrongCount;

    @ApiModelProperty("正确率")
    private Integer accuracyRate;

    @ApiModelProperty("较上一周期正确率变化")
    private Integer accuracyRateDelta;
}
