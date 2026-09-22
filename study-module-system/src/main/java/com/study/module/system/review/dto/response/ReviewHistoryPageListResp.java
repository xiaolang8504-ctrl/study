package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 复习历史分页响应
 */
@Data
public class ReviewHistoryPageListResp {

    @ApiModelProperty("复习记录ID")
    private Long id;

    @ApiModelProperty("错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("题目标题快照")
    private String questionTitle;

    @ApiModelProperty("科目字典键值快照")
    private String subject;

    @ApiModelProperty("科目名称快照")
    private String subjectName;

    @ApiModelProperty("反馈")
    private Integer feedback;

    @ApiModelProperty("反馈文本")
    private String feedbackText;

    @ApiModelProperty("学生本次答案")
    private String studentAnswer;

    @ApiModelProperty("是否正确: 0错误, 1正确")
    private Integer isCorrect;

    @ApiModelProperty("判定来源: 0学生自评, 1系统自动判定")
    private Integer answerJudgeType;

    @ApiModelProperty("主动回忆用时，单位秒")
    private Integer answerDurationSeconds;

    @ApiModelProperty("是否逾期")
    private Integer isOverdue;

    @ApiModelProperty("复习前阶段")
    private Integer stageBefore;

    @ApiModelProperty("复习后阶段")
    private Integer stageAfter;

    @ApiModelProperty("复习前掌握度")
    private Integer masteryScoreBefore;

    @ApiModelProperty("复习后掌握度")
    private Integer masteryScoreAfter;

    @ApiModelProperty("本次掌握度变化")
    private Integer masteryScoreDelta;

    @ApiModelProperty("反馈后连续正确次数")
    private Integer correctStreakAfter;

    @ApiModelProperty("反馈后连续错误次数")
    private Integer wrongStreakAfter;

    @ApiModelProperty("复习后间隔，单位分钟")
    private Integer intervalAfterMinutes;

    @ApiModelProperty("复习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    @ApiModelProperty("下次复习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextReviewTime;
}
