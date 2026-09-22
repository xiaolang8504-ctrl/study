package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相似题推荐曝光及作答记录。
 */
@Data
@TableName("sys_question_recommendation_log")
public class QuestionRecommendationLog {
    /**
     * 推荐记录ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 单次推荐批次号。
     */
    private String batchNo;

    /**
     * 学生用户ID。
     */
    private Long userId;

    /**
     * 推荐来源错题ID。
     */
    private Long wrongQuestionId;

    /**
     * 被推荐的题库题目ID。
     */
    private Long bankQuestionId;

    /**
     * 题目在推荐批次中的排序。
     */
    private Integer rankNo;

    /**
     * 推荐匹配分数。
     */
    private Double recommendScore;

    /**
     * 推荐算法版本。
     */
    private String algorithmVersion;

    /**
     * 候选召回方式。
     */
    private String matchType;

    /**
     * 面向学生的推荐原因。
     */
    private String recommendReason;

    /**
     * 推荐A/B实验分组。
     */
    private String experimentGroup;

    /**
     * 实验配置ID；旧数据为空。
     */
    private Long experimentId;

    /**
     * 题目曝光时间。
     */
    private LocalDateTime exposureTime;

    /**
     * 学生提交答案。
     */
    private String studentAnswer;

    /**
     * 是否回答正确：0错误，1正确，未作答为空。
     */
    private Integer isCorrect;

    /**
     * 判题方式：AUTO自动，SELF学生自评。
     */
    private String judgeType;

    /**
     * 本次作答耗时秒数。
     */
    private Integer durationSeconds;

    /**
     * 主观题查看标准答案时间。
     */
    private LocalDateTime answerViewedTime;

    /**
     * 答案提交时间。
     */
    private LocalDateTime answerTime;

    /**
     * 记录创建时间。
     */
    private LocalDateTime createTime;
}
