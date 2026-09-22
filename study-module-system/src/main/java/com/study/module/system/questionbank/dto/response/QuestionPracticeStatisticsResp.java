package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 题目练习统计响应
 */
@Data
public class QuestionPracticeStatisticsResp {

    @ApiModelProperty("推荐曝光数量")
    private Long exposureCount;

    @ApiModelProperty("已作答数量")
    private Long answeredCount;

    @ApiModelProperty("答对数量")
    private Long correctCount;

    @ApiModelProperty("举报数量")
    private Long reportCount;

    @ApiModelProperty("作答率")
    private Double answerRate;

    @ApiModelProperty("正确率")
    private Double correctRate;

    @ApiModelProperty("A组已作答数量")
    private Long groupAAnsweredCount;

    @ApiModelProperty("A组正确率")
    private Double groupACorrectRate;

    @ApiModelProperty("B组已作答数量")
    private Long groupBAnsweredCount;

    @ApiModelProperty("B组正确率")
    private Double groupBCorrectRate;

    @ApiModelProperty("当前实验配置")
    private QuestionExperimentResp experiment;

    @ApiModelProperty("B组相对A组正确率提升，百分点")
    private Double lift;

    @ApiModelProperty("双样本比例检验Z值")
    private Double zScore;

    @ApiModelProperty("双侧P值")
    private Double pValue;

    @ApiModelProperty("是否达到95%显著性")
    private Boolean significant;
}
