package com.study.module.system.review.service;

import com.study.module.system.review.entity.ReviewPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 复习提醒单次发送服务
 */
public interface ReviewReminderSendService {

    /**
     * 在独立事务中发送一个计划的提醒。
     */
    void sendReviewReminder(ReviewPlan plan, LocalDate today, LocalDateTime now);

    /**
     * 在新事务中记录提醒发送失败信息。
     */
    void recordSendFailure(ReviewPlan plan, LocalDate today, LocalDateTime now, Throwable throwable);
}
