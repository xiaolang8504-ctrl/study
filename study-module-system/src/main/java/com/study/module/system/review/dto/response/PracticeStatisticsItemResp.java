package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专项练习统计项响应
 */
@Data
public class PracticeStatisticsItemResp {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("题目数量")
    private Integer questionCount;

    @ApiModelProperty("正确数量")
    private Integer correctCount;

    @ApiModelProperty("错误数量")
    private Integer wrongCount;

    @ApiModelProperty("正确率")
    private Integer accuracyRate;
}
