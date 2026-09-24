package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReviewExamSprintResp {
    private Long id;
    private String subject;
    private LocalDate examDate;
    private String scopeText;
    private Integer dailyMinutes;
    private Integer targetQuestionCount;
    private Integer daysRemaining;
    private Integer estimatedMinutes;
    private String coordinationNote;
    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        private Long wrongQuestionId;
        private String questionTitle;
        private String learningPoint;
        private String priorityReason;
        private Integer estimatedMinutes;
    }
}
