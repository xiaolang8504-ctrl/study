package com.study.module.system.questionbank.dto.response;

import lombok.Data;
@Data
public class QuestionVariantTemplateResp {
    private Long id; private String templateCode; private String templateName; private String templateVersion; private Long originalQuestionId;
    private String grade; private String subject; private String questionType; private String questionPattern;
    private String answerFormula; private String variableSchemaJson; private String analysisPattern; private Integer difficulty;
    private Long primaryKnowledgePointId; private String primaryKnowledgePointName; private Integer enable;
}
