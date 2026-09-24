package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 今日复习首页响应类
 */
@Data
public class ReviewTodayHomeResp {

    @ApiModelProperty("复习日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reviewDate;

    @ApiModelProperty("今日任务总数")
    private Long totalCount;

    @ApiModelProperty("今日已完成数量")
    private Long completedCount;

    @ApiModelProperty("今日剩余数量")
    private Long remainingCount;

    @ApiModelProperty("逾期数量")
    private Long overdueCount;

    @ApiModelProperty("预计剩余用时，单位分钟")
    private Integer estimatedMinutes;

    @ApiModelProperty("今日完成进度")
    private BigDecimal progressRate;

    @ApiModelProperty("连续复习天数")
    private Integer continuousReviewDays;

    @ApiModelProperty("每日复习题量上限")
    private Integer dailyLimit;

    @ApiModelProperty("今日待复习题单")
    private List<ReviewTodayTaskResp> taskList;

    @ApiModelProperty("当前筛选科目，空表示全部")
    private String selectedSubject;

    @ApiModelProperty("可参与复习的科目")
    private List<ReviewSubjectSettingResp> subjectSettings;

    @ApiModelProperty("可解释复习计划")
    private ReviewPlanExplanationResp planExplanation;
}
