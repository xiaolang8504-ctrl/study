package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 主观题自评申诉及教师复核记录。
 */
@Data
@TableName("sys_question_practice_appeal")
public class QuestionPracticeAppeal {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 推荐记录ID
     */
    private Long recommendationId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 题库题目ID
     */
    private Long bankQuestionId;
    /**
     * 申诉原因
     */
    private String appealReason;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 复核是否正确
     */
    private Integer reviewCorrect;
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
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
