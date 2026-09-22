package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题复习历史记录实体
 */
@Data
public class ReviewRecord {

    /**
     * 复习记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户端幂等请求ID
     */
    private String requestId;

    /**
     * 复习计划ID
     */
    private Long planId;

    /**
     * 复习项目ID
     */
    private Long reviewItemId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 错题ID
     */
    private Long wrongQuestionId;

    /**
     * 复习时题目标题快照
     */
    private String questionTitleSnapshot;

    /**
     * 复习时科目字典键值快照
     */
    private String subject;

    /**
     * 复习时科目名称快照
     */
    private String subjectNameSnapshot;

    /**
     * 本次原计划复习时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 开始查看题目时间
     */
    private LocalDateTime startTime;

    /**
     * 查看答案时间
     */
    private LocalDateTime revealTime;

    /**
     * 提交反馈时间
     */
    private LocalDateTime reviewTime;

    /**
     * 反馈: 0忘记, 1困难, 2掌握, 3很简单
     */
    private Integer feedback;

    /**
     * 主动回忆用时，单位秒
     */
    private Integer answerDurationSeconds;

    /**
     * 学生本次答案快照
     */
    private String studentAnswer;

    /**
     * 本次作答是否正确: 0错误, 1正确
     */
    private Integer isCorrect;

    /** 是否有答案曝光前保存、且与最终提交一致的主动回忆凭证：0否，1是。 */
    private Integer isIndependent;

    /**
     * 判定来源: 0学生自评, 1系统自动判定
     */
    private Integer answerJudgeType;

    /**
     * 是否逾期: 0否, 1是
     */
    private Integer isOverdue;

    /**
     * 复习前阶段
     */
    private Integer stageBefore;

    /**
     * 复习后阶段
     */
    private Integer stageAfter;

    /**
     * 复习前掌握度，0-100
     */
    private Integer masteryScoreBefore;

    /**
     * 复习后掌握度，0-100
     */
    private Integer masteryScoreAfter;

    /**
     * 本次掌握度变化
     */
    private Integer masteryScoreDelta;

    /**
     * 反馈后连续正确次数
     */
    private Integer correctStreakAfter;

    /**
     * 反馈后连续错误次数
     */
    private Integer wrongStreakAfter;

    /**
     * 复习前间隔，单位分钟
     */
    private Integer intervalBeforeMinutes;

    /**
     * 复习后间隔，单位分钟
     */
    private Integer intervalAfterMinutes;

    /**
     * 计算后的下次复习时间
     */
    private LocalDateTime nextReviewTime;

    /**
     * 计算使用的算法版本
     */
    private String algorithmVersion;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
