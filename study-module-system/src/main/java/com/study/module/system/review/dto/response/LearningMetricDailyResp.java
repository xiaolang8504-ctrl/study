package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学情报告趋势中的单日快照。
 */
@Data
public class LearningMetricDailyResp {

    @ApiModelProperty("快照日期")
    private LocalDate metricDate;
    @ApiModelProperty("错题总数")
    private Integer wrongQuestionCount;
    @ApiModelProperty("已掌握题数")
    private Integer masteredCount;
    @ApiModelProperty("掌握率，百分比")
    private Integer masteryRate;
    @ApiModelProperty("当天完成复习数")
    private Integer reviewCount;
    @ApiModelProperty("当天已判定独立作答数")
    private Integer judgedAnswerCount;
    @ApiModelProperty("当天独立作答正确数")
    private Integer correctAnswerCount;
    @ApiModelProperty("当天保持率，百分比；无判定样本时为 0")
    private Integer retentionRate;
}
