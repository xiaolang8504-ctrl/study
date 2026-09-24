package com.study.module.system.review.constants;

import com.study.module.system.review.dto.response.ReviewFeedbackProjectionResp;
import com.study.module.system.review.entity.ReviewRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 将既有间隔复习规则转换为学生可读的解释；不参与提交时的真实状态更新。
 */
public final class ReviewPlanExplanationPolicy {

    private static final int DEFAULT_MASTERY_SCORE = 40;

    private static final int DEFAULT_STAGE = ReviewStage.INITIAL;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MM月dd日 HH:mm");

    private ReviewPlanExplanationPolicy() {
    }

    /**
     * 预览学生在答案曝光前独立作答时，不同反馈会如何影响下一次复习。
     */
    public static ReviewFeedbackProjectionResp project(Integer masteryScore, Integer stage,
                                                        int feedback, LocalDateTime baseTime) {
        int scoreBefore = masteryScore == null ? DEFAULT_MASTERY_SCORE : masteryScore;
        int stageBefore = stage == null ? DEFAULT_STAGE : stage;
        int scoreAfter;
        int stageAfter;
        if (feedback == ReviewFeedback.FORGOT) {
            scoreAfter = clamp(scoreBefore - 20);
            stageAfter = ReviewStage.MIN;
        } else if (feedback == ReviewFeedback.DIFFICULT) {
            scoreAfter = clamp(scoreBefore - 10);
            stageAfter = Math.max(ReviewStage.MIN, stageBefore - 1);
        } else {
            scoreAfter = clamp(scoreBefore + (feedback == ReviewFeedback.EASY ? 20 : 15));
            stageAfter = Math.min(ReviewStage.MAX,
                    stageBefore + (feedback == ReviewFeedback.EASY ? 2 : 1));
        }
        int interval = intervalMinutesByMastery(scoreAfter);
        ReviewFeedbackProjectionResp response = new ReviewFeedbackProjectionResp();
        response.setFeedback(feedback);
        response.setFeedbackName(feedbackName(feedback));
        response.setMasteryScoreAfter(scoreAfter);
        response.setStageAfter(stageAfter);
        response.setIntervalMinutes(interval);
        response.setNextReviewTime(baseTime.plusMinutes(interval));
        response.setExplanation(projectionExplanation(feedback, scoreBefore, scoreAfter,
                response.getNextReviewTime()));
        return response;
    }

    /** 生成一次已提交反馈的实际排期说明。 */
    public static String nextReviewReason(ReviewRecord record) {
        String nextTime = record.getNextReviewTime() == null ? "后续计划" :
                TIME_FORMATTER.format(record.getNextReviewTime());
        if (!Integer.valueOf(1).equals(record.getIsCorrect())) {
            return "本次作答判定为错误，掌握度 " + value(record.getMasteryScoreBefore()) + "→"
                    + value(record.getMasteryScoreAfter()) + "，已安排在 " + nextTime + " 重新复习。";
        }
        if (!Integer.valueOf(1).equals(record.getIsIndependent())) {
            return "本次答案未在查看解析前保存，不计入独立掌握；已安排在 " + nextTime + " 再次巩固。";
        }
        return "本次独立作答正确，掌握度 " + value(record.getMasteryScoreBefore()) + "→"
                + value(record.getMasteryScoreAfter()) + "，按“" + feedbackName(record.getFeedback())
                + "”反馈安排在 " + nextTime + " 复习。";
    }

    private static String projectionExplanation(int feedback, int scoreBefore, int scoreAfter,
                                                LocalDateTime nextReviewTime) {
        String next = TIME_FORMATTER.format(nextReviewTime);
        if (feedback == ReviewFeedback.FORGOT) {
            return "答错或选择忘记时，掌握度 " + scoreBefore + "→" + scoreAfter
                    + "，预计 " + next + " 再次复习。";
        }
        return "在查看答案前独立答对的前提下，选择“" + feedbackName(feedback)
                + "”会使掌握度 " + scoreBefore + "→" + scoreAfter + "，预计 " + next + " 复习。";
    }

    private static String feedbackName(Integer feedback) {
        if (feedback == null) {
            return "本次反馈";
        }
        switch (feedback) {
            case ReviewFeedback.FORGOT:
                return "忘记";
            case ReviewFeedback.DIFFICULT:
                return "困难";
            case ReviewFeedback.MASTERED:
                return "掌握";
            case ReviewFeedback.EASY:
                return "很简单";
            default:
                return "本次反馈";
        }
    }

    private static int intervalMinutesByMastery(int masteryScore) {
        if (masteryScore <= 30) {
            return 24 * 60;
        }
        if (masteryScore <= 60) {
            return 3 * 24 * 60;
        }
        if (masteryScore <= 80) {
            return 7 * 24 * 60;
        }
        if (masteryScore <= 95) {
            return 15 * 24 * 60;
        }
        return 30 * 24 * 60;
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private static String value(Integer value) {
        return value == null ? "--" : String.valueOf(value);
    }
}
