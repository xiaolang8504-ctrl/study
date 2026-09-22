package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 错题复习提醒任务实体
 */
@Data
public class ReviewReminder {

    /**
     * 提醒任务ID
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
     * 提醒日期
     */
    private LocalDate reminderDate;

    /**
     * 提醒类型
     */
    private String reminderType;

    /**
     * 今日到期数量
     */
    private Integer dueCount;

    /**
     * 逾期数量
     */
    private Integer overdueCount;

    /**
     * 各科任务数量摘要
     */
    private String subjectSummary;

    /**
     * 计划发送时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 发送状态: 0待发送, 1已发送, 2发送失败
     */
    private Integer sendStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 生成的站内消息ID
     */
    private Long msgId;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 实际发送时间
     */
    private LocalDateTime sentTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
