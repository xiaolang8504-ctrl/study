package com.study.module.system.questionbank.dto.response;

import lombok.Data;

/** 知识点前置关系响应。 */
@Data
public class KnowledgePointPrerequisiteResp {
    private Long id;
    private Long knowledgePointId;
    private String knowledgePointName;
    private Long prerequisitePointId;
    private String prerequisitePointName;
    private String relationVersion;
    private Integer enable;
}
