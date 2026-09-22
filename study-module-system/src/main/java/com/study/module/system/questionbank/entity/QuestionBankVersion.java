package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库题目版本快照。
 */
@Data
@TableName("sys_question_bank_version")
public class QuestionBankVersion {
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
     * 版本号
     */
    private Integer versionNo;
    /**
     * 操作类型
     */
    private String operationType;
    /**
     * 快照JSON
     */
    private String snapshotJson;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
