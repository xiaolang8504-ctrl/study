package com.study.module.system.questionbank.dto.response;

import lombok.Data;
@Data
public class QuestionVariantGenerateResp {
    private Long recordId; private Long questionBankId; private String generatedContent; private String generatedAnswer;
    private String variableValuesJson; private Integer validationStatus; private String validationDetail; private Integer reviewStatus;
}
