package com.study.module.system.questionbank.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 保存知识点前置关系请求。 */
@Data
public class KnowledgePointPrerequisiteSaveReq {
    private Long id;
    @NotNull(message = "知识点不能为空") private Long knowledgePointId;
    @NotNull(message = "前置知识点不能为空") private Long prerequisitePointId;
    @Size(max = 50, message = "关系版本不能超过50个字符") private String relationVersion;
    private Integer enable;
}
