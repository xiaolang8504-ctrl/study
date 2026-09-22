package com.study.module.system.review.service;

/**
 * 复习提醒更新服务
 */
public interface ReviewReminderUpdateService {

    /**
     * 标记复习提醒已读
     */
    void readReviewReminder(Long id);
}
