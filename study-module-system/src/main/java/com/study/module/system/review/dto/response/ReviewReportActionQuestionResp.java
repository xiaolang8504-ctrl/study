package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学情报告行动下钻中的错题摘要。
 */
@Data
public class ReviewReportActionQuestionResp {

    @ApiModelProperty("个人错题ID，可直接加入组卷篮")
    private Long wrongQuestionId;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("知识点文本")
    private String learningPoint;

    @ApiModelProperty("错因标签")
    private String errorLabels;

    @ApiModelProperty("错题状态")
    private Integer status;

    @ApiModelProperty("难度等级")
    private Integer level;

    @ApiModelProperty("最近一次有效独立作答时间")
    private LocalDateTime lastReviewTime;

    @ApiModelProperty("近30天有效独立作答次数")
    private Integer independentReviewCount30Days;

    @ApiModelProperty("近30天有效独立作答正确次数")
    private Integer independentCorrectCount30Days;

    @ApiModelProperty("当前累计复习次数")
    private Integer reviewCount;

    @ApiModelProperty("当前遗忘次数")
    private Integer lapseCount;

    @ApiModelProperty("当前掌握度，0-100")
    private Integer masteryScore;

    @ApiModelProperty("本题入选行动清单的原因")
    private String selectedReason;
}
