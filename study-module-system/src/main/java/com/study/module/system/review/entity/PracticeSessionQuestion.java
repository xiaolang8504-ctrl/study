package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专项练习题目实体
 */
@Data
@TableName("sys_practice_session_question")
public class PracticeSessionQuestion {

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
     * 题目序号
     */
    private Integer sortNo;

    /**
     * 题目标题快照
     */
    private String questionTitleSnapshot;

    private String questionContentSnapshot;
    private String contentFormatSnapshot;
    private String optionsJsonSnapshot;
    private String assetSnapshotJson;
    private String correctAnswerSnapshot;
    private String analysisSnapshot;

    /**
     * 入选依据快照
     */
    private String sourceReason;

    /**
     * 科目字典键值
     */
    private String subject;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 知识点
     */
    private String learningPoint;

    /**
     * 错因标签快照
     */
    private String errorLabelSnapshot;

    /**
     * 难度
     */
    private Integer difficulty;

    /**
     * 学生答案
     */
    private String studentAnswer;

    /**
     * 是否正确
     */
    private Integer isCorrect;

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
