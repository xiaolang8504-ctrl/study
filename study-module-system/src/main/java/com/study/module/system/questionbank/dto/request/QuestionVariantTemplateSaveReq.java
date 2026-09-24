package com.study.module.system.questionbank.dto.request;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class QuestionVariantTemplateSaveReq {
    private Long id;
    @NotBlank(message = "模板编码不能为空") @Size(max = 80, message = "模板编码不能超过80个字符") private String templateCode;
    @NotBlank(message = "模板名称不能为空") @Size(max = 100, message = "模板名称不能超过100个字符") private String templateName;
    @NotNull(message = "原题不能为空") private Long originalQuestionId;
    @Size(max = 50, message = "模板版本不能超过50个字符") private String templateVersion;
    @NotBlank(message = "年级不能为空") private String grade;
    @NotBlank(message = "科目不能为空") private String subject;
    @NotBlank(message = "题型不能为空") private String questionType;
    @NotBlank(message = "题干模板不能为空") private String questionPattern;
    @NotBlank(message = "答案公式不能为空") private String answerFormula;
    @NotBlank(message = "变量规则不能为空") private String variableSchemaJson;
    private String analysisPattern;
    @NotNull(message = "难度不能为空") @Min(1) @Max(5) private Integer difficulty;
    @NotNull(message = "主知识点不能为空") private Long primaryKnowledgePointId;
    private Integer enable;
}
