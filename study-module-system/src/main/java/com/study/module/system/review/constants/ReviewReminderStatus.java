package com.study.module.system.review.constants;

/**
 * 复习提醒发送状态
 */
public final class ReviewReminderStatus {

    public static final int PENDING = 0;

    public static final int SENT = 1;

    public static final int FAILED = 2;

    /**
     * 执行 ReviewReminderStatus 辅助处理。
     */
    private ReviewReminderStatus() {
    }
}
