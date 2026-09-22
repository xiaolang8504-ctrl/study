package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题复习排期实体
 */
@Data
public class ReviewItem {

    /**
     * 复习项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 复习计划ID
     */
    private Long planId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 错题ID
     */
    private Long wrongQuestionId;

    /**
     * 项目状态: 0正常, 1暂停, 2结束
     */
    private Integer itemStatus;

    /**
     * 当前复习阶段: 0-7
     */
    private Integer stage;

    /**
     * 当前复习间隔，单位分钟
     */
    private Integer currentIntervalMinutes;

    /**
     * 最近反馈: 0忘记, 1困难, 2掌握, 3很简单
     */
    private Integer lastFeedback;

    /**
     * 当前掌握度，0-100
     */
    private Integer masteryScore;

    /**
     * 连续掌握次数
     */
    private Integer correctStreak;

    /**
     * 连续错误次数
     */
    private Integer wrongStreak;

    /**
     * 遗忘次数
     */
    private Integer lapseCount;

    /**
     * 累计复习次数
     */
    private Integer reviewCount;

    /**
     * 最近复习时间
     */
    private LocalDateTime lastReviewTime;

    /**
     * 下次复习时间
     */
    private LocalDateTime nextReviewTime;

    /**
     * 达到掌握状态时间
     */
    private LocalDateTime masteredTime;

    /**
     * 当前算法版本
     */
    private String algorithmVersion;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
