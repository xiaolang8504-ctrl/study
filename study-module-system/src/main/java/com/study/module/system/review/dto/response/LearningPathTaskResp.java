package com.study.module.system.review.dto.response;

import lombok.Data;

/** 学习路径中的一个可解释节点。 */
@Data
public class LearningPathTaskResp {
    private String stage;
    private Long knowledgePointId;
    private String knowledgePointName;
    private String reason;
    private Integer evidenceSampleCount;
    private Integer confidence;
}
