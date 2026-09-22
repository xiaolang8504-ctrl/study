package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewDefault;
import com.study.module.system.review.constants.ReviewFeedback;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewStage;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.PracticeProgressSyncService;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 专项练习进度同步服务实现
 */
@Service
public class PracticeProgressSyncServiceImpl implements PracticeProgressSyncService {

    private static final int DEFAULT_MASTERY_SCORE = 40;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    /**
     * 同步练习。
     */
    @Override
    public void syncPracticeAnswer(Long wrongQuestionId, Long userId, boolean correct) {
        WrongQuestion wrongQuestion = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getId, wrongQuestionId)
                .eq(WrongQuestion::getCreateId, userId)
                .one();
        if (wrongQuestion == null || !isReviewable(wrongQuestion.getStatus())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!correct && Integer.valueOf(WrongQuestionStatus.MASTERED).equals(wrongQuestion.getStatus())) {
            wrongQuestionService.lambdaUpdate()
                    .eq(WrongQuestion::getId, wrongQuestionId)
                    .eq(WrongQuestion::getCreateId, userId)
                    .set(WrongQuestion::getStatus, WrongQuestionStatus.CORRECTED)
                    .set(WrongQuestion::getUpdateTime, now)
                    .update();
            wrongQuestion.setStatus(WrongQuestionStatus.CORRECTED);
            wrongQuestion.setUpdateTime(now);
        }
        ReviewItem reviewItem = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId)
                .eq(ReviewItem::getWrongQuestionId, wrongQuestionId)
                .one();
        if (reviewItem == null) {
            reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
            reviewItem = reviewItemService.lambdaQuery()
                    .eq(ReviewItem::getUserId, userId)
                    .eq(ReviewItem::getWrongQuestionId, wrongQuestionId)
                    .one();
        }
        if (reviewItem == null) {
            return;
        }
        // 专项练习不保存答案曝光前凭证，也没有跨间隔复习记录，因此只调整复习计划。
        applyPracticeResult(reviewItem, correct, now);
    }

    /**
     * 专项练习只处理已订正和已掌握错题，答错会重新激活复习项。
     */
    private boolean isReviewable(Integer status) {
        return Integer.valueOf(WrongQuestionStatus.CORRECTED).equals(status)
                || Integer.valueOf(WrongQuestionStatus.MASTERED).equals(status);
    }

    private void applyPracticeResult(ReviewItem reviewItem, boolean correct, LocalDateTime now) {
        int scoreBefore = defaultValue(reviewItem.getMasteryScore(),
                stageToMasteryScore(defaultValue(reviewItem.getStage(), ReviewStage.INITIAL)));
        int stageBefore = defaultValue(reviewItem.getStage(), ReviewStage.INITIAL);
        int correctStreakBefore = defaultValue(reviewItem.getCorrectStreak(), 0);
        int wrongStreakBefore = defaultValue(reviewItem.getWrongStreak(), 0);
        int lapseCountBefore = defaultValue(reviewItem.getLapseCount(), 0);
        int reviewCountBefore = defaultValue(reviewItem.getReviewCount(), 0);

        int scoreAfter;
        int stageAfter;
        int correctStreakAfter;
        int wrongStreakAfter;
        int lapseCountAfter;
        int feedback;
        LocalDateTime nextReviewTime;
        LocalDateTime masteredTime = reviewItem.getMasteredTime();
        if (correct) {
            scoreAfter = clamp(scoreBefore + 8);
            stageAfter = Math.min(stageBefore + 1, ReviewStage.MAX);
            correctStreakAfter = correctStreakBefore + 1;
            wrongStreakAfter = 0;
            lapseCountAfter = lapseCountBefore;
            feedback = ReviewFeedback.MASTERED;
            nextReviewTime = now.plusMinutes(intervalMinutesByMastery(scoreAfter));
        } else {
            scoreAfter = clamp(scoreBefore - 15);
            stageAfter = Math.max(ReviewStage.MIN, stageBefore - 1);
            correctStreakAfter = 0;
            wrongStreakAfter = wrongStreakBefore + 1;
            lapseCountAfter = lapseCountBefore + 1;
            feedback = ReviewFeedback.FORGOT;
            nextReviewTime = now;
            masteredTime = null;
        }

        reviewItemService.lambdaUpdate()
                .eq(ReviewItem::getId, reviewItem.getId())
                .eq(ReviewItem::getUserId, reviewItem.getUserId())
                .set(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                .set(ReviewItem::getStage, stageAfter)
                .set(ReviewItem::getCurrentIntervalMinutes, intervalMinutesByMastery(scoreAfter))
                .set(ReviewItem::getLastFeedback, feedback)
                .set(ReviewItem::getMasteryScore, scoreAfter)
                .set(ReviewItem::getCorrectStreak, correctStreakAfter)
                .set(ReviewItem::getWrongStreak, wrongStreakAfter)
                .set(ReviewItem::getLapseCount, lapseCountAfter)
                .set(ReviewItem::getReviewCount, reviewCountBefore + 1)
                .set(ReviewItem::getLastReviewTime, now)
                .set(ReviewItem::getNextReviewTime, nextReviewTime)
                .set(ReviewItem::getMasteredTime, masteredTime)
                .set(ReviewItem::getAlgorithmVersion, ReviewDefault.ALGORITHM_VERSION)
                .set(ReviewItem::getUpdateTime, now)
                .setSql("version = version + 1")
                .update();
    }

    /**
     * 处理业务数据。
     */
    private int stageToMasteryScore(int stage) {
        return clamp(DEFAULT_MASTERY_SCORE + Math.max(0, stage - ReviewStage.INITIAL) * 8);
    }

    /**
     * 执行 intervalMinutesByMastery 辅助处理。
     */
    private int intervalMinutesByMastery(int masteryScore) {
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

    /**
     * 标准化并计算业务数据。
     */
    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 标准化并计算业务数据。
     */
    private int defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
