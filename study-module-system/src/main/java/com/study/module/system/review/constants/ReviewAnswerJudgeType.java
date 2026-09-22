package com.study.module.system.review.constants;

/**
 * 复习答案判定来源
 */
public final class ReviewAnswerJudgeType {

    /**
     * 学生对照标准答案后自评
     */
    public static final int SELF = 0;

    /**
     * 系统对可精确匹配题型自动判定
     */
    public static final int AUTO = 1;

    private ReviewAnswerJudgeType() {
    }
}
