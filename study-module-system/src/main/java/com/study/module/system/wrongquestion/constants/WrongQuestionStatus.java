package com.study.module.system.wrongquestion.constants;

/**
 * 错题状态
 */
public final class WrongQuestionStatus {

    /**
     * 待订正
     */
    public static final int PENDING_CORRECTION = 0;

    /**
     * 已订正
     */
    public static final int CORRECTED = 1;

    /**
     * 已掌握
     */
    public static final int MASTERED = 2;

    /**
     * 已归档
     */
    public static final int ARCHIVED = 3;

    private WrongQuestionStatus() {
    }

    public static boolean valid(Integer status) {
        return Integer.valueOf(PENDING_CORRECTION).equals(status)
                || Integer.valueOf(CORRECTED).equals(status)
                || Integer.valueOf(MASTERED).equals(status)
                || Integer.valueOf(ARCHIVED).equals(status);
    }
}
