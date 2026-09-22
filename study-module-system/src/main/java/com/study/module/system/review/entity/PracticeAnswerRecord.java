package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专项练习作答记录实体
 */
@Data
@TableName("sys_practice_answer_record")
public class PracticeAnswerRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 练习会话ID
     */
    private Long sessionId;

    /**
     * 练习题目ID
     */
    private Long sessionQuestionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 错题ID
     */
    private Long wrongQuestionId;

    /**
     * 题目来源
     */
    private String questionSource;

    /**
     * 题库题目ID
     */
    private Long bankQuestionId;

    /**
     * 学生答案
     */
    private String studentAnswer;

    /**
     * 是否正确
     */
    private Integer isCorrect;

    /**
     * 判题方式
     */
    private Integer judgeType;

    /**
     * 作答用时，秒
     */
    private Integer durationSeconds;

    /**
     * 作答时间
     */
    private LocalDateTime answerTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
