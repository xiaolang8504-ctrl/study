package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 题目内容治理留痕，覆盖问题处置、版权凭证及授权边界变更。
 */
@Data
@TableName("sys_question_content_governance")
public class QuestionContentGovernance {
    @TableId(type = IdType.AUTO)
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
