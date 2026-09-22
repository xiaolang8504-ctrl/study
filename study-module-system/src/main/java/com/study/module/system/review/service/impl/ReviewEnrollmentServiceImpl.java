package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewDefault;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewPlanStatus;
import com.study.module.system.review.constants.ReviewStage;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.mapper.ReviewItemMapper;
import com.study.module.system.review.mapper.ReviewPlanMapper;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.review.service.ReviewPlanService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题加入智能复习计划服务实现
 */
@Service
public class ReviewEnrollmentServiceImpl implements ReviewEnrollmentService {

    @Autowired
    ReviewPlanService reviewPlanService;

    @Autowired
    ReviewPlanMapper reviewPlanMapper;

    @Autowired
    ReviewItemMapper reviewItemMapper;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    ReviewSubjectSettingService reviewSubjectSettingService;

    /**
     * 同步错题复习任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncWrongQuestionReview(WrongQuestion wrongQuestion) {
        // 入口可能由新增、编辑、归档等多种操作触发，先过滤无主键或无归属的无效数据。
        if (wrongQuestion == null || wrongQuestion.getId() == null || wrongQuestion.getCreateId() == null) {
            return;
        }
        LocalDateTime baseTime = wrongQuestion.getUpdateTime() == null
                ? LocalDateTime.now() : wrongQuestion.getUpdateTime();
        ReviewItem existingItem = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, wrongQuestion.getCreateId())
                .eq(ReviewItem::getWrongQuestionId, wrongQuestion.getId())
                .one();
        // 待订正题暂停复习，已归档题结束复习，避免不完整题目继续出现在今日任务中。
        if (!isReviewableStatus(wrongQuestion.getStatus())) {
            pauseOrFinishReviewItem(existingItem, wrongQuestion.getStatus(), baseTime);
            return;
        }
        ReviewPlan reviewPlan = getOrCreateReviewPlan(wrongQuestion.getCreateId(), baseTime);
        boolean subjectEnabled = reviewSubjectSettingService.isSubjectEnabled(
                reviewPlan, wrongQuestion.getSubject());
        // 首次进入复习计划时按错题状态创建阶段，并遵循当前科目的启停设置。
        if (existingItem == null) {
            ReviewItem reviewItem = buildReviewItem(reviewPlan.getId(), wrongQuestion, baseTime);
            reviewItem.setItemStatus(subjectEnabled
                    ? ReviewItemStatus.NORMAL : ReviewItemStatus.PAUSED);
            reviewItemMapper.insertIgnoreReviewItem(reviewItem);
            return;
        }
        if (!subjectEnabled) {
            pauseReviewItem(existingItem, baseTime);
            return;
        }
        boolean resumeItem = !Integer.valueOf(ReviewItemStatus.NORMAL).equals(existingItem.getItemStatus());
        boolean restartMasteredItem = Integer.valueOf(1).equals(wrongQuestion.getStatus())
                && existingItem.getMasteredTime() != null;
        // 已掌握题被重新标记为已订正，说明学生需要重新学习，必须清空旧进度重新排期。
        if (resumeItem || restartMasteredItem) {
            ReviewItem resetItem = buildReviewItem(reviewPlan.getId(), wrongQuestion, baseTime);
            resetReviewItem(existingItem, resetItem);
            return;
        }
        if (Integer.valueOf(2).equals(wrongQuestion.getStatus())
                && existingItem.getMasteredTime() == null) {
            markReviewItemMastered(existingItem, baseTime);
        }
    }

    /**
     * 完成错题复习任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishWrongQuestions(List<Long> wrongQuestionIds, Long userId) {
        if (wrongQuestionIds == null || wrongQuestionIds.isEmpty()) {
            return;
        }
        // 删除错题时保留历史记录，但将关联复习项统一结束，防止产生孤立任务。
        reviewItemService.lambdaUpdate()
                .eq(ReviewItem::getUserId, userId)
                .in(ReviewItem::getWrongQuestionId, wrongQuestionIds)
                .set(ReviewItem::getItemStatus, ReviewItemStatus.FINISHED)
                .set(ReviewItem::getUpdateTime, LocalDateTime.now())
                .update();
    }

    /**
     * 每名学生只保留一个复习计划，并通过唯一索引处理并发初始化。
     */
    private ReviewPlan getOrCreateReviewPlan(Long userId, LocalDateTime now) {
        ReviewPlan reviewPlan = reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getUserId, userId)
                .one();
        if (reviewPlan != null) {
            return reviewPlan;
        }
        ReviewPlan newPlan = new ReviewPlan();
        newPlan.setUserId(userId);
        newPlan.setPlanName(ReviewDefault.PLAN_NAME);
        newPlan.setDailyLimit(ReviewDefault.DAILY_LIMIT);
        newPlan.setReminderEnabled(ReviewDefault.REMINDER_ENABLED);
        newPlan.setReminderTime(ReviewDefault.REMINDER_TIME);
        newPlan.setReviewWeekDays(ReviewDefault.REVIEW_WEEK_DAYS);
        newPlan.setAlgorithmVersion(ReviewDefault.ALGORITHM_VERSION);
        newPlan.setStatus(ReviewPlanStatus.ENABLED);
        newPlan.setCreateTime(now);
        newPlan.setUpdateTime(now);
        reviewPlanMapper.insertIgnoreReviewPlan(newPlan);
        return reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getUserId, userId)
                .one();
    }

    /**
     * 已订正错题从阶段1开始并在一天后首次复习；已掌握错题保留掌握阶段。
     */
    private ReviewItem buildReviewItem(Long planId, WrongQuestion wrongQuestion, LocalDateTime baseTime) {
        boolean mastered = Integer.valueOf(2).equals(wrongQuestion.getStatus());
        int intervalMinutes = mastered
                ? ReviewDefault.MASTERED_INTERVAL_MINUTES : ReviewDefault.INITIAL_INTERVAL_MINUTES;
        ReviewItem reviewItem = new ReviewItem();
        reviewItem.setPlanId(planId);
        reviewItem.setUserId(wrongQuestion.getCreateId());
        reviewItem.setWrongQuestionId(wrongQuestion.getId());
        reviewItem.setItemStatus(ReviewItemStatus.NORMAL);
        reviewItem.setStage(mastered ? ReviewDefault.MASTERED_STAGE : ReviewStage.INITIAL);
        reviewItem.setCurrentIntervalMinutes(intervalMinutes);
        reviewItem.setMasteryScore(mastered ? 90 : 40);
        reviewItem.setCorrectStreak(0);
        reviewItem.setWrongStreak(0);
        reviewItem.setLapseCount(0);
        reviewItem.setReviewCount(0);
        reviewItem.setNextReviewTime(baseTime.plusMinutes(intervalMinutes));
        reviewItem.setMasteredTime(mastered ? baseTime : null);
        reviewItem.setAlgorithmVersion(ReviewDefault.ALGORITHM_VERSION);
        reviewItem.setVersion(0);
        reviewItem.setCreateTime(baseTime);
        reviewItem.setUpdateTime(baseTime);
        return reviewItem;
    }

    /**
     * 判断是否为可复习状态
     */
    private boolean isReviewableStatus(Integer status) {
        // 只有已订正和已掌握状态具备完整答案，允许进入主动回忆流程。
        return Integer.valueOf(1).equals(status) || Integer.valueOf(2).equals(status);
    }

    /**
     * 根据错题状态暂停或结束复习项
     */
    private void pauseOrFinishReviewItem(ReviewItem reviewItem, Integer wrongQuestionStatus,
                                         LocalDateTime updateTime) {
        if (reviewItem == null) {
            return;
        }
        int itemStatus = Integer.valueOf(3).equals(wrongQuestionStatus)
                ? ReviewItemStatus.FINISHED : ReviewItemStatus.PAUSED;
        reviewItemService.lambdaUpdate()
                .eq(ReviewItem::getId, reviewItem.getId())
                .eq(ReviewItem::getUserId, reviewItem.getUserId())
                .set(ReviewItem::getItemStatus, itemStatus)
                .set(ReviewItem::getUpdateTime, updateTime)
                .update();
    }

    /**
     * 暂停复习项
     */
    private void pauseReviewItem(ReviewItem reviewItem, LocalDateTime updateTime) {
        if (reviewItem == null || Integer.valueOf(ReviewItemStatus.PAUSED)
                .equals(reviewItem.getItemStatus())) {
            return;
        }
        reviewItemService.lambdaUpdate()
                .eq(ReviewItem::getId, reviewItem.getId())
                .eq(ReviewItem::getUserId, reviewItem.getUserId())
                .set(ReviewItem::getItemStatus, ReviewItemStatus.PAUSED)
                .set(ReviewItem::getUpdateTime, updateTime)
                .update();
    }

    /**
     * 恢复暂停或结束的任务时重新开始排期，并显式清空旧反馈和旧复习时间。
     */
    private void resetReviewItem(ReviewItem existingItem, ReviewItem resetItem) {
        reviewItemService.lambdaUpdate()
                .eq(ReviewItem::getId, existingItem.getId())
                .eq(ReviewItem::getUserId, existingItem.getUserId())
                .set(ReviewItem::getPlanId, resetItem.getPlanId())
                .set(ReviewItem::getItemStatus, resetItem.getItemStatus())
                .set(ReviewItem::getStage, resetItem.getStage())
                .set(ReviewItem::getCurrentIntervalMinutes, resetItem.getCurrentIntervalMinutes())
                .set(ReviewItem::getLastFeedback, null)
                .set(ReviewItem::getMasteryScore, resetItem.getMasteryScore())
                .set(ReviewItem::getCorrectStreak, 0)
                .set(ReviewItem::getWrongStreak, 0)
                .set(ReviewItem::getLapseCount, 0)
                .set(ReviewItem::getReviewCount, 0)
                .set(ReviewItem::getLastReviewTime, null)
                .set(ReviewItem::getNextReviewTime, resetItem.getNextReviewTime())
                .set(ReviewItem::getMasteredTime, resetItem.getMasteredTime())
                .set(ReviewItem::getAlgorithmVersion, resetItem.getAlgorithmVersion())
                .set(ReviewItem::getVersion, 0)
                .set(ReviewItem::getUpdateTime, resetItem.getUpdateTime())
                .update();
    }

    /**
     * 标记复习项已掌握
     */
    private void markReviewItemMastered(ReviewItem reviewItem, LocalDateTime masteredTime) {
        // 外部直接把错题标记为已掌握时，同步复习阶段和下次巩固时间，保持两边状态一致。
        reviewItemService.lambdaUpdate()
                .eq(ReviewItem::getId, reviewItem.getId())
                .eq(ReviewItem::getUserId, reviewItem.getUserId())
                .set(ReviewItem::getStage, ReviewDefault.MASTERED_STAGE)
                .set(ReviewItem::getCurrentIntervalMinutes, ReviewDefault.MASTERED_INTERVAL_MINUTES)
                .set(ReviewItem::getMasteryScore, 90)
                .set(ReviewItem::getWrongStreak, 0)
                .set(ReviewItem::getNextReviewTime,
                        masteredTime.plusMinutes(ReviewDefault.MASTERED_INTERVAL_MINUTES))
                .set(ReviewItem::getMasteredTime, masteredTime)
                .set(ReviewItem::getUpdateTime, masteredTime)
                .update();
    }
}
