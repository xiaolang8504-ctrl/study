package com.study.module.system.review.dto.response;

import com.study.module.system.wrongquestion.dto.response.WrongQuestionErrorAnalysisStatisticsResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 学情报告响应。
 */
@Data
public class ReviewLearningReportResp {

    @ApiModelProperty("统计科目，为空表示全部科目")
    private String subject;

    @ApiModelProperty("错题总数")
    private Integer wrongQuestionCount;

    @ApiModelProperty("待订正数量")
    private Integer pendingCorrectionCount;

    @ApiModelProperty("已订正、复习中数量")
    private Integer correctedCount;

    @ApiModelProperty("已掌握数量")
    private Integer masteredCount;

    @ApiModelProperty("已归档数量")
    private Integer archivedCount;

    @ApiModelProperty("掌握率，已掌握题数占全部错题的百分比")
    private Integer masteryRate;

    @ApiModelProperty("近30天复习次数")
    private Integer reviewCount30Days;

    @ApiModelProperty("近30天新增错题数")
    private Integer newWrongQuestionCount30Days;

    @ApiModelProperty("近30天完成的到期复习数")
    private Integer dueReviewCompletedCount30Days;

    @ApiModelProperty("近30天答案曝光前独立作答且判定正确的次数")
    private Integer independentCorrectCount30Days;

    @ApiModelProperty("当前仍逾期、可继续处理的复习任务数")
    private Integer overdueReviewCount;

    @ApiModelProperty("近30天有独立作答凭证的保持率")
    private Integer retentionRate30Days;

    @ApiModelProperty("近30天保持率的有效作答样本数")
    private Integer retentionSampleCount30Days;

    @ApiModelProperty("近30天保持率是否达到最小样本量")
    private Boolean retentionRateReliable;

    @ApiModelProperty("薄弱知识点TOP10")
    private List<WrongQuestionKnowledgePointStatisticsResp> weakLearningPointList;

    @ApiModelProperty("错因分布")
    private List<WrongQuestionErrorAnalysisStatisticsResp> errorAnalysisList;

    @ApiModelProperty("近30日学习快照趋势；快照从 P1 发布后的次日开始积累")
    private List<LearningMetricDailyResp> dailyMetricList;
}
