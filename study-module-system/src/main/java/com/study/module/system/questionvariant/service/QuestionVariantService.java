package com.study.module.system.questionvariant.service;

import com.study.module.system.questionbank.dto.request.QuestionVariantGenerateReq;
import com.study.module.system.questionbank.dto.request.QuestionVariantTemplateSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionVariantGenerateResp;
import com.study.module.system.questionbank.dto.response.QuestionVariantTemplateResp;
import java.util.List;

public interface QuestionVariantService {
    void saveQuestionVariantTemplate(QuestionVariantTemplateSaveReq request);
    List<QuestionVariantTemplateResp> questionVariantTemplateList(String subject);
    QuestionVariantGenerateResp generateQuestionVariant(QuestionVariantGenerateReq request);
}
