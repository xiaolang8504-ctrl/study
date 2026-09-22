package com.study.module.system.guardian.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 家长查看的学生学习概览。刻意不包含题干、图片、学生作答、答案或复习反馈。
 */
@Data
public class GuardianStudentOverviewResp {

    private Long studentUserId;
    private String studentName;
    private Integer todayDueCount;
    private Integer overdueCount;
    private Integer sevenDayReviewCount;
    private Integer wrongQuestionCount;
    private Integer correctionRate;
    private Integer independentCorrectRate;
    private List<GuardianWeakPointResp> weakPointList;
    private String weeklySuggestion;
}
