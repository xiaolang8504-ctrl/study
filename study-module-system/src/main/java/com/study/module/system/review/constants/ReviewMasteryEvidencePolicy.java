package com.study.module.system.review.constants;

import com.study.module.system.review.entity.ReviewRecord;

import java.time.LocalDateTime;
import java.util.List;

/** 已掌握状态要求连续三次跨排期的独立正确作答。 */
public final class ReviewMasteryEvidencePolicy {

    private ReviewMasteryEvidencePolicy() {
    }

    /** 历史记录须按复习时间从新到旧排列，仅检查最近两次。 */
    public static boolean hasSpacedIndependentCorrectHistory(List<ReviewRecord> history,
                                                               LocalDateTime currentReviewTime) {
        if (currentReviewTime == null || history == null || history.size() != 2) {
            return false;
        }
        LocalDateTime nextTime = currentReviewTime;
        for (ReviewRecord record : history) {
            if (record == null || !Integer.valueOf(1).equals(record.getIsIndependent())
                    || !Integer.valueOf(1).equals(record.getIsCorrect())
                    || record.getReviewTime() == null || record.getNextReviewTime() == null
                    || nextTime.isBefore(record.getNextReviewTime())) {
                return false;
            }
            nextTime = record.getReviewTime();
        }
        return true;
    }
}
