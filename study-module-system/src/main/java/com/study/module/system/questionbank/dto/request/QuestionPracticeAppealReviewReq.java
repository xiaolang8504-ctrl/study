package com.study.module.system.questionbank.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 主观题作答申诉审核请求
 */
@Data
public class QuestionPracticeAppealReviewReq {
    @NotNull(message = "申诉ID不能为空")
    private Long id;
    @NotNull(message = "教师复核结果不能为空")
    private Boolean correct;
    @NotBlank(message = "复核说明不能为空")
    private String reviewRemark;
}
