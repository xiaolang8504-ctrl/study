package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式题记录。
 */
@Data
@TableName("sys_question_variant_record")
public class QuestionVariantRecord {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 批次ID
     */
    private Long batchId;
    /**
     * 模板ID
     */
    private Long templateId;
    /**
     * 模板版本号
     */
    private Integer templateVersion;
    /**
     * 原始题目ID
     */
    private Long originalQuestionId;
    /**
     * 题库题目ID
     */
    private Long bankQuestionId;
    /**
     * 来源记录ID
     */
    private Long sourceRecordId;
    /**
     * 题目内容
     */
    private String questionContent;
    /**
     * 正确答案
     */
    private String correctAnswer;
    /**
     * 题目解析
     */
    private String analysis;
    /**
     * 题目哈希
     */
    private String questionHash;
    /**
     * 校验状态
     */
    private String validationStatus;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 审核备注
     */
    private String auditRemark;
    /**
     * 创建人ID
     */
    private Long createId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
