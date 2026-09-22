package com.study.module.system.review.constants;

import com.study.module.system.review.entity.ReviewRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewMetricPolicyTest {

    @Test
    void shouldExcludeAnswersWithoutIndependentEvidence() {
        ReviewRecord copied = new ReviewRecord();
        copied.setIsIndependent(0);
        copied.setIsCorrect(1);
        ReviewRecord verified = new ReviewRecord();
        verified.setIsIndependent(1);
        verified.setIsCorrect(1);
        assertFalse(ReviewMetricPolicy.isEffectiveReview(copied));
        assertFalse(ReviewMetricPolicy.isIndependentCorrect(copied));
        assertTrue(ReviewMetricPolicy.isEffectiveReview(verified));
        assertTrue(ReviewMetricPolicy.isIndependentCorrect(verified));
        assertEquals(0, ReviewMetricPolicy.percent(0, 0));
        assertEquals(50, ReviewMetricPolicy.percent(1, 2));
    }
}
