package com.study.module.system.questionbank.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 主观题作答申诉列表响应
 */
@Data
public class QuestionPracticeAppealListResp {
    private Long id;
    private Long recommendationId;
    private Long userId;
    private Long bankQuestionId;
    private String questionTitle;
    private String studentAnswer;
    private String correctAnswer;
    private Integer selfCorrect;
    private String appealReason;
    private Integer status;
    private Integer reviewCorrect;
    private String reviewRemark;
    private Long reviewerId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
