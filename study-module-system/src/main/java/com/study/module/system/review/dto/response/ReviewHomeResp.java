package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 智能复习首页响应类
 */
@Data
public class ReviewHomeResp {

    @ApiModelProperty("复习计划ID")
    private Long planId;

    @ApiModelProperty("复习计划名称")
    private String planName;

    @ApiModelProperty("每日复习题量上限")
    private Integer dailyLimit;

    @ApiModelProperty("是否开启提醒: 0否, 1是")
    private Integer reminderEnabled;

    @ApiModelProperty("提醒时间，格式HH:mm")
    private String reminderTime;

    @ApiModelProperty("今日任务总数")
    private Long todayTaskCount;

    @ApiModelProperty("今日已完成数量")
    private Long completedCount;

    @ApiModelProperty("当前剩余数量")
    private Long remainingCount;

    @ApiModelProperty("逾期数量")
    private Long overdueCount;

    @ApiModelProperty("预计剩余用时，单位分钟")
    private Integer estimatedMinutes;

    @ApiModelProperty("连续复习天数")
    private Integer continuousReviewDays;

    @ApiModelProperty("当前学生错题总数，作为总体掌握率分母")
    private Long reviewQuestionCount;

    @ApiModelProperty("已掌握错题数量")
    private Long masteredCount;

    @ApiModelProperty("总体掌握率")
    private BigDecimal masteryRate;

    @ApiModelProperty("平均掌握度")
    private BigDecimal averageMasteryScore;

    @ApiModelProperty("未来7天排期")
    private List<ReviewScheduleResp> scheduleList;

    @ApiModelProperty("知识点掌握统计TOP5")
    private List<ReviewLearningPointResp> learningPointList;

    @ApiModelProperty("当前筛选科目，空表示全部")
    private String selectedSubject;

    @ApiModelProperty("可参与复习的科目")
    private List<ReviewSubjectSettingResp> subjectSettings;

    @ApiModelProperty("按10/20/30分钟组织的今日行动任务包")
    private List<ReviewTodayActionPackageResp> todayActionPackageList;
}
