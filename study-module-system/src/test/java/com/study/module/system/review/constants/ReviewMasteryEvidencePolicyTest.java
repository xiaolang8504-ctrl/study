package com.study.module.system.review.constants;

import com.study.module.system.review.entity.ReviewRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewMasteryEvidencePolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 21, 19, 0);

    @Test
    void shouldRequireTwoPriorIndependentCorrectAnswersAfterTheirDueTimes() {
        ReviewRecord latest = record(NOW.minusDays(7), NOW.minusDays(1), 1, 1);
        ReviewRecord older = record(NOW.minusDays(21), NOW.minusDays(14), 1, 1);

        assertTrue(ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(
                Arrays.asList(latest, older), NOW));
        assertFalse(ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(
                Collections.singletonList(latest), NOW));
        assertFalse(ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(
                Arrays.asList(latest, record(NOW.minusDays(21), NOW.minusDays(14), null, 1)), NOW));
        assertFalse(ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(
                Arrays.asList(latest, record(NOW.minusDays(21), NOW.minusDays(14), 1, 0)), NOW));
    }

    @Test
    void shouldNotCountEarlyOrSameIntervalRepetition() {
        ReviewRecord latest = record(NOW.minusHours(1), NOW.plusDays(1), 1, 1);
        ReviewRecord older = record(NOW.minusDays(2), NOW.minusDays(1), 1, 1);
        assertFalse(ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(
                Arrays.asList(latest, older), NOW));

        latest.setNextReviewTime(NOW.minusMinutes(1));
        older.setNextReviewTime(NOW.minusMinutes(30));
        assertFalse(ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(
                Arrays.asList(latest, older), NOW));
    }

    private ReviewRecord record(LocalDateTime reviewedAt, LocalDateTime nextDueAt,
                                Integer independent, Integer correct) {
        ReviewRecord record = new ReviewRecord();
        record.setReviewTime(reviewedAt);
        record.setNextReviewTime(nextDueAt);
        record.setIsIndependent(independent);
        record.setIsCorrect(correct);
        return record;
    }
}
