package com.study.api.provider;

/**
 * 智能复习提醒服务
 */
public interface ReviewReminderProvider {

    /**
     * 派发当前到期的站内复习提醒。
     */
    void dispatchDueReviewReminder();
}
