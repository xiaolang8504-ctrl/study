package com.study.module.system.guardian.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 家长周计划与学生待办确认状态。 */
@Data
public class GuardianWeeklyPlanResp {
    private Long id;
    private Long guardianUserId;
    private Long studentUserId;
    private String studentName;
    private LocalDate weekStartDate;
    private String planTitle;
    private String planContent;
    private Integer targetReviewCount;
    private String reportSuggestionSnapshot;
    private Integer todoStatus;
    private LocalDateTime todoRequestTime;
    private LocalDateTime studentConfirmTime;
    private LocalDateTime updateTime;
}
