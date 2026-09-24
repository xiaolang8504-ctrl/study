package com.study.module.system.questionbank.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class QuestionVariantGenerateReq {
    @NotNull(message = "模板不能为空") private Long templateId;
    /** 相同种子和模板版本会得到相同变量，便于复现与审计。 */
    private Long seed;
    private Long sourceQuestionId;
}
