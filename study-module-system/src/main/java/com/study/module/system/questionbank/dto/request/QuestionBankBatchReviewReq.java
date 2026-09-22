package com.study.module.system.questionbank.dto.request;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 题库批量审核请求。
 */
@Data
public class QuestionBankBatchReviewReq {
    @NotEmpty(message = "请选择需要审核的题目")
    private List<Long> ids;
    @NotNull(message = "审核状态不能为空")
    @Min(1) @Max(2)
    private Integer reviewStatus;
    private String reviewRemark;
}
