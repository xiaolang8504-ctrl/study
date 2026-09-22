package com.study.module.system.review.constants;

import java.time.LocalTime;

/**
 * 智能复习默认配置
 */
public final class ReviewDefault {

    public static final String PLAN_NAME = "我的错题复习";

    public static final int DAILY_LIMIT = 20;

    public static final int SUBJECT_DAILY_LIMIT = 5;

    public static final int REMINDER_ENABLED = 1;

    public static final LocalTime REMINDER_TIME = LocalTime.of(19, 30);

    public static final String REVIEW_WEEK_DAYS = "1,2,3,4,5,6,7";

    public static final String ALGORITHM_VERSION = "stage-v2-evidence";

    public static final int INITIAL_INTERVAL_MINUTES = 1440;

    public static final int MASTERED_STAGE = 5;

    public static final int MASTERED_INTERVAL_MINUTES = 43200;

    public static final int ESTIMATED_MINUTES_PER_QUESTION = 2;

    /**
     * 执行 ReviewDefault 辅助处理。
     */
    private ReviewDefault() {
    }
}
