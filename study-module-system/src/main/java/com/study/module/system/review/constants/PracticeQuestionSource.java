package com.study.module.system.review.constants;

/**
 * 专项练习题目来源
 */
public final class PracticeQuestionSource {

    public static final String WRONG_QUESTION = "WRONG_QUESTION";

    public static final String QUESTION_BANK = "QUESTION_BANK";

    public static final String MIXED = "MIXED";

    /**
     * 执行 PracticeQuestionSource 辅助处理。
     */
    private PracticeQuestionSource() {
    }

    /**
     * 校验错题。
     */
    public static boolean includeWrongQuestion(String source) {
        return source == null || source.isEmpty() || WRONG_QUESTION.equals(source) || MIXED.equals(source);
    }

    /**
     * 校验题库。
     */
    public static boolean includeQuestionBank(String source) {
        return QUESTION_BANK.equals(source) || MIXED.equals(source);
    }
}
