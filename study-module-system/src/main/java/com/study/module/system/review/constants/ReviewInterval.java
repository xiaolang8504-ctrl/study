package com.study.module.system.review.constants;

/**
 * 复习阶段间隔配置，单位分钟
 */
public final class ReviewInterval {

    public static final int FORGOT_MINUTES = 10;

    public static final int DIFFICULT_MINUTES = 30;

    private static final int[] STAGE_INTERVAL_MINUTES = {
            10,
            1440,
            4320,
            10080,
            20160,
            43200,
            86400,
            172800
    };

    /**
     * 执行 stageIntervalMinutes 业务处理。
     */
    public static int stageIntervalMinutes(int stage) {
        int safeStage = Math.max(ReviewStage.MIN, Math.min(stage, ReviewStage.MAX));
        return STAGE_INTERVAL_MINUTES[safeStage];
    }

    private ReviewInterval() {
    }
}
