package com.study.module.system.questionbank.dto.response;

import lombok.Data;

/**
 * 主观题标准答案解锁响应。
 */
@Data
public class QuestionPracticeAnswerViewResp {
    private String correctAnswer;
    private String analysis;
}
