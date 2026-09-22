package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生题目纠错举报。
 */
@Data
@TableName("sys_question_report")
public class QuestionReport {
    /**
     * 举报记录ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 举报学生用户ID。
     */
    private Long userId;

    /**
     * 被举报的题库题目ID。
     */
    private Long bankQuestionId;

    /**
     * 举报类型编码。
     */
    private String reportType;

    /**
     * 举报问题描述。
     */
    private String reportContent;

    /**
     * 处理状态：0待处理，1已处理，2无效。
     */
    private Integer status;

    /**
     * 处理人用户ID。
     */
    private Long handlerId;

    /**
     * 处理备注。
     */
    private String handleRemark;

    /**
     * 处理时间。
     */
    private LocalDateTime handleTime;

    /**
     * 举报时间。
     */
    private LocalDateTime createTime;
}
