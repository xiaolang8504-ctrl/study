package com.study.module.system.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 家长与单个学生沟通的周计划；不包含题目、答案和掌握反馈。 */
@Data
@TableName("guardian_weekly_plan")
public class GuardianWeeklyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long guardianUserId;
    private Long studentUserId;
    private LocalDate weekStartDate;
    private String planTitle;
    private String planContent;
    private Integer targetReviewCount;
    private String reportSuggestionSnapshot;
    private Integer todoStatus;
    private Long studentConfirmUserId;
    private LocalDateTime todoRequestTime;
    private LocalDateTime studentConfirmTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
