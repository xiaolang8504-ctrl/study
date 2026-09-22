package com.study.module.system.review.service;

/**
 * 复习提醒派发服务
 */
public interface ReviewReminderDispatchService {

    /**
     * 派发到期复习提醒
     */
    void dispatchDueReviewReminder();
}
