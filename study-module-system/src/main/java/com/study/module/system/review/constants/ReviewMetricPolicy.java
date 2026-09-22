package com.study.module.system.review.constants;

import com.study.module.system.review.entity.ReviewRecord;

/** 学生首页、学情报告和家长汇总共用的学习指标口径。 */
public final class ReviewMetricPolicy {

    private ReviewMetricPolicy() {
    }

    /** 有效复习必须有答案曝光前的独立作答及正确性判定。 */
    public static boolean isEffectiveReview(ReviewRecord record) {
        return record != null && Integer.valueOf(1).equals(record.getIsIndependent())
                && record.getIsCorrect() != null;
    }

    public static boolean isIndependentCorrect(ReviewRecord record) {
        return isEffectiveReview(record) && Integer.valueOf(1).equals(record.getIsCorrect());
    }

    public static int percent(long numerator, long denominator) {
        return denominator == 0 ? 0 : (int) Math.round(numerator * 100.0 / denominator);
    }
}
