package com.study.module.system.wrongquestion.dto.response;

import lombok.Data;

/** 错题重复/相似候选。 */
@Data
public class WrongQuestionDuplicateResp {
    private Long relationId;
    private Long questionId;
    private String questionTitle;
    private String matchType;
    private Integer similarityScore;
    private String status;
}
