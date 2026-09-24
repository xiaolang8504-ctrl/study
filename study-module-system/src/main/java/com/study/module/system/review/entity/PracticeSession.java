package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专项练习会话实体
 */
@Data
@TableName("sys_practice_session")
public class PracticeSession {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 练习类型
     */
    private String practiceType;

    /**
     * 标题
     */
    private String title;

    /**
     * 组卷规则快照
     */
    private String generationReason;

    /**
     * 每题作答留白行数
     */
    private Integer blankLineCount;

    /**
     * 答案位置：AFTER_EACH逐题显示，END卷末集中显示
     */
    private String answerPosition;

    /**
     * 题面图片：ORIGINAL原图，GRAYSCALE灰度预览，TEXT_ONLY仅文字。
     */
    private String imageMode;

    /** 练习卷版本。 */
    private Integer paperVersion;

    /** 排版栏数：1或2。 */
    private Integer columnCount;

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
     * 错因标签
     */
    private String errorLabel;

    /**
     * 难度
     */
    private Integer difficulty;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 已答数量
     */
    private Integer answeredCount;

    /**
     * 正确数量
     */
    private Integer correctCount;

    /**
     * 错误数量
     */
    private Integer wrongCount;

    /**
     * 总用时，秒
     */
    private Integer totalDurationSeconds;

    /**
     * 正确率，0-100
     */
    private Integer accuracyRate;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
