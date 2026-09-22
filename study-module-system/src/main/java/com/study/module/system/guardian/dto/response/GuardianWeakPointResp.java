package com.study.module.system.guardian.dto.response;

import lombok.Data;

/** 家长可见的薄弱知识点聚合，不包含原题和答案。 */
@Data
public class GuardianWeakPointResp {

    private String learningPoint;
    private Integer wrongQuestionCount;
    private Integer pendingCorrectionCount;
}
