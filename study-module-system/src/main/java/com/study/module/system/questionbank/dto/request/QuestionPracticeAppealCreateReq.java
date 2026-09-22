package com.study.module.system.questionbank.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 主观题作答申诉创建请求
 */
@Data
public class QuestionPracticeAppealCreateReq {
    @NotNull(message = "推荐记录ID不能为空")
    private Long recommendationId;
    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;
}
