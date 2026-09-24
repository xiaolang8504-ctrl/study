package com.study.module.system.questionbank.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 题目内容治理记录。 */
@Data
public class QuestionContentGovernanceResp {
    private Long id;
    private Long questionId;
    private String action;
    private String issueType;
    private String handleRemark;
    private String proofFileIds;
    private String licenseVersion;
    private LocalDate expireAt;
    private Integer targetVersionNo;
    private Long operatorId;
    private LocalDateTime createTime;
}
