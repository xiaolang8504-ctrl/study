package com.study.module.system.review.constants;

/**
 * 智能复习缓存键
 */
public final class ReviewCacheKey {

    public static final String ANSWER_REVEAL = "REVIEW:ANSWER:REVEAL:";

    /** 服务端确认“先作答、后查看答案”的草稿凭证。 */
    public static final String ANSWER_DRAFT = "REVIEW:ANSWER:DRAFT:";

    public static final long ANSWER_REVEAL_TTL_SECONDS = 7200L;

    public static String answerRevealKey(Long userId, Long reviewItemId) {
        return ANSWER_REVEAL + userId + ":" + reviewItemId;
    }

    public static String answerDraftKey(Long userId, Long reviewItemId) {
        return ANSWER_DRAFT + userId + ":" + reviewItemId;
    }

    /**
     * 执行 ReviewCacheKey 辅助处理。
     */
    private ReviewCacheKey() {
    }
}
