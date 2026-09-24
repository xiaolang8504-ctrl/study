package com.study.module.system.review.constants;

/**
 * 纸面练习题回填状态。
 */
public final class PracticePaperAnswerStatus {

    public static final String CORRECT = "CORRECT";

    public static final String WRONG = "WRONG";

    public static final String UNANSWERED = "UNANSWERED";

    private PracticePaperAnswerStatus() {
    }

    public static boolean valid(String status) {
        return CORRECT.equals(status) || WRONG.equals(status) || UNANSWERED.equals(status);
    }

    public static boolean isAnswered(String status) {
        return CORRECT.equals(status) || WRONG.equals(status);
    }

    public static boolean isCorrect(String status) {
        return CORRECT.equals(status);
    }
}
