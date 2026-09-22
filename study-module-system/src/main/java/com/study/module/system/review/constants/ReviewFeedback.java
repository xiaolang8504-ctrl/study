package com.study.module.system.review.constants;

/**
 * 复习反馈
 */
public final class ReviewFeedback {

    public static final int FORGOT = 0;

    public static final int DIFFICULT = 1;

    public static final int MASTERED = 2;

    public static final int EASY = 3;

    /**
     * 校验相关业务数据。
     */
    public static boolean isValid(Integer feedback) {
        return feedback != null && feedback >= FORGOT && feedback <= EASY;
    }

    private ReviewFeedback() {
    }
}
