package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库审核日志。
 */
@Data
@TableName("sys_question_bank_review_log")
public class QuestionBankReviewLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 题目ID
     */
    private Long questionId;
    /**
     * 审核状态
     */
    private Integer reviewStatus;
    /**
     * 审核备注
     */
    private String reviewRemark;
    /**
     * 审核人ID
     */
    private Long reviewerId;
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;
}
