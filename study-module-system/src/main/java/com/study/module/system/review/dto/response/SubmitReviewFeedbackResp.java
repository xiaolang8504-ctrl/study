package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交复习反馈响应类
 */
@Data
public class SubmitReviewFeedbackResp {

    @ApiModelProperty("复习记录ID")
    private Long reviewRecordId;

    @ApiModelProperty("本次反馈")
    private Integer feedback;

    @ApiModelProperty("反馈后阶段")
    private Integer stageAfter;

    @ApiModelProperty("反馈前掌握度")
    private Integer masteryScoreBefore;

    @ApiModelProperty("反馈后掌握度")
    private Integer masteryScoreAfter;

    @ApiModelProperty("本次掌握度变化")
    private Integer masteryScoreDelta;

    @ApiModelProperty("反馈后间隔，单位分钟")
    private Integer intervalAfterMinutes;

    @ApiModelProperty("连续掌握次数")
    private Integer correctStreak;

    @ApiModelProperty("连续错误次数")
    private Integer wrongStreak;

    @ApiModelProperty("下次复习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextReviewTime;

    @ApiModelProperty("本次反馈形成该排期的原因")
    private String nextReviewReason;

    @ApiModelProperty("是否已掌握")
    private Boolean mastered;
}
