package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 专项练习统计响应
 */
@Data
public class PracticeStatisticsResp {

    @ApiModelProperty("练习次数")
    private Integer sessionCount;

    @ApiModelProperty("已完成练习次数")
    private Integer finishedSessionCount;

    @ApiModelProperty("题目数量")
    private Integer questionCount;

    @ApiModelProperty("已答数量")
    private Integer answeredCount;

    @ApiModelProperty("正确数量")
    private Integer correctCount;

    @ApiModelProperty("错误数量")
    private Integer wrongCount;

    @ApiModelProperty("平均正确率")
    private Integer averageAccuracyRate;

    @ApiModelProperty("总用时，秒")
    private Integer totalDurationSeconds;

    @ApiModelProperty("薄弱知识点")
    private List<PracticeStatisticsItemResp> weakLearningPointList;

    @ApiModelProperty("错因分布")
    private List<PracticeStatisticsItemResp> errorLabelList;

    @ApiModelProperty("练习趋势")
    private List<PracticeTrendResp> trendList;
}
