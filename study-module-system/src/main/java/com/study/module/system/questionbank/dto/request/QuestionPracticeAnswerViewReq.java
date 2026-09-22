package com.study.module.system.questionbank.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 主观题提交作答并查看标准答案请求。
 */
@Data
public class QuestionPracticeAnswerViewReq {
    @NotNull(message = "推荐记录ID不能为空")
    private Long recommendationId;
    @NotBlank(message = "学生答案不能为空")
    private String studentAnswer;
    private Integer durationSeconds;
}
