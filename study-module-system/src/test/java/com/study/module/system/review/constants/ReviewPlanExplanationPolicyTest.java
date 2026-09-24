package com.study.module.system.review.constants;

import com.study.module.system.review.dto.response.ReviewFeedbackProjectionResp;
import com.study.module.system.review.entity.ReviewRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 可解释计划的反馈预估与实际说明测试。 */
class ReviewPlanExplanationPolicyTest {

    @Test
    void shouldProjectForgotToNextDayForLowMastery() {
        LocalDateTime base = LocalDateTime.of(2026, 9, 24, 10, 0);

        ReviewFeedbackProjectionResp projection = ReviewPlanExplanationPolicy.project(
                40, ReviewStage.INITIAL, ReviewFeedback.FORGOT, base);

        assertEquals(20, projection.getMasteryScoreAfter());
        assertEquals(24 * 60, projection.getIntervalMinutes());
        assertEquals(base.plusDays(1), projection.getNextReviewTime());
        assertTrue(projection.getExplanation().contains("答错"));
    }

    @Test
    void shouldExplainActualWrongAnswerAsResetInsteadOfPositiveFeedback() {
        ReviewRecord record = new ReviewRecord();
        record.setIsCorrect(0);
        record.setMasteryScoreBefore(55);
        record.setMasteryScoreAfter(35);
        record.setNextReviewTime(LocalDateTime.of(2026, 9, 25, 10, 0));

        String explanation = ReviewPlanExplanationPolicy.nextReviewReason(record);

        assertTrue(explanation.contains("作答判定为错误"));
        assertTrue(explanation.contains("55→35"));
    }
}
