package com.study.module.system.guardian.dto.response;
import lombok.Data;
/** 家长周报仅含学习汇总，不含题目答案或学生作答。 */
@Data public class GuardianWeeklyReportResp {
    private Long studentUserId; private String studentName; private Integer newWrongQuestionCount;
    private Integer correctedCount; private Integer effectiveReviewCount; private Integer retentionRate;
    private Integer overdueCount; private String nextWeekSuggestion;
}
