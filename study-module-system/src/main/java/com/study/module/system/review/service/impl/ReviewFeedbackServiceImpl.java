package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewDefault;
import com.study.module.system.review.constants.ReviewAnswerJudgeType;
import com.study.module.system.review.constants.ReviewCacheKey;
import com.study.module.system.review.constants.ReviewFeedback;
import com.study.module.system.review.constants.ReviewInterval;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewStage;
import com.study.module.system.review.constants.ReviewMasteryEvidencePolicy;
import com.study.module.system.review.constants.ReviewPlanExplanationPolicy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.review.dto.request.SubmitReviewFeedbackReq;
import com.study.module.system.review.dto.response.SubmitReviewFeedbackResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.mapper.ReviewItemMapper;
import com.study.module.system.review.service.ReviewFeedbackService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import com.yunshang.budget.common.redis.RedisService;
import cn.hutool.crypto.SecureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Set;

/**
 * 复习反馈服务实现
 */
@Service
public class ReviewFeedbackServiceImpl implements ReviewFeedbackService {

    private static final int MASTERED_STREAK = 3;

    private static final int DEFAULT_MASTERY_SCORE = 40;

    private static final int MASTERED_SCORE = 90;

    private static final Set<String> AUTO_JUDGE_QUESTION_TYPES = new HashSet<>(Arrays.asList(
            "选择题", "判断题", "填空题", "单选题", "多选题"));

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    ReviewItemMapper reviewItemMapper;

    @Autowired
    ReviewRecordService reviewRecordService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    @Autowired
    RedisService redisService;

    /**
     * 提交复习反馈
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitReviewFeedbackResp submitReviewFeedback(SubmitReviewFeedbackReq request) {
        Long userId = AccountUtils.getUserId();

        // requestId 在一次答题过程中保持不变，重复提交时直接返回首次处理结果，避免重复推进复习阶段。
        ReviewRecord existingRecord = reviewRecordService.lambdaQuery()
                .eq(ReviewRecord::getRequestId, request.getRequestId())
                .one();
        if (existingRecord != null) {
            if (!userId.equals(existingRecord.getUserId())) {
                throw new LogicException(ErrorCodeConstants.REVIEW_FEEDBACK_CONFLICT);
            }
            return buildIdempotentResponse(existingRecord, userId);
        }

        // 先确认任务归属，再使用任务版本校验“先查看答案再反馈”的服务端凭证。
        ReviewItem reviewItem = reviewItemService.checkReviewItem(request.getReviewItemId(), userId);
        if (!Integer.valueOf(ReviewItemStatus.NORMAL).equals(reviewItem.getItemStatus())) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ITEM_NOT_AVAILABLE);
        }
        LocalDateTime revealTime = validateAndGetRevealTime(request, userId, reviewItem);
        WrongQuestion wrongQuestion = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getId, reviewItem.getWrongQuestionId())
                .eq(WrongQuestion::getCreateId, userId)
                .one();
        if (wrongQuestion == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ITEM_NOT_EXIST);
        }

        // 先保存更新前快照，后续用于算法计算和生成不可变的复习历史记录。
        LocalDateTime reviewTime = LocalDateTime.now();
        int stageBefore = defaultValue(reviewItem.getStage(), ReviewStage.INITIAL);
        int intervalBefore = defaultValue(reviewItem.getCurrentIntervalMinutes(),
                ReviewInterval.stageIntervalMinutes(stageBefore));
        int masteryScoreBefore = defaultValue(reviewItem.getMasteryScore(),
                stageToMasteryScore(stageBefore));
        int expectedVersion = defaultValue(reviewItem.getVersion(), 0);
        LocalDateTime scheduledTime = reviewItem.getNextReviewTime();
        AnswerResult answerResult = resolveAnswerResult(request, wrongQuestion);
        boolean independent = hasIndependentAnswerProof(userId, reviewItem.getId(),
                request.getStudentAnswer(), revealTime);
        // 查看答案后补填的正确答案只留作练习记录，不推进掌握度或连续正确次数。
        int effectiveFeedback = answerResult.correct == 1
                ? request.getFeedback() : ReviewFeedback.FORGOT;
        ReviewDecision decision = answerResult.correct == 1 && !independent
                ? new ReviewDecision(stageBefore, ReviewDefault.INITIAL_INTERVAL_MINUTES,
                        masteryScoreBefore, 0, defaultValue(reviewItem.getWrongStreak(), 0),
                        defaultValue(reviewItem.getLapseCount(), 0))
                : calculateDecision(effectiveFeedback, stageBefore,
                        defaultValue(reviewItem.getCorrectStreak(), 0),
                        defaultValue(reviewItem.getWrongStreak(), 0),
                        defaultValue(reviewItem.getLapseCount(), 0),
                        masteryScoreBefore);
        LocalDateTime nextReviewTime = reviewTime.plusMinutes(decision.intervalMinutes);
        // 已掌握必须有连续三次跨排期的独立正确记录；旧记录缺证据时保守地不晋级。
        boolean mastered = Integer.valueOf(2).equals(wrongQuestion.getStatus())
                || independent && answerResult.correct == 1
                && decision.masteryScore >= MASTERED_SCORE
                && decision.correctStreak >= MASTERED_STREAK
                && decision.stage >= ReviewDefault.MASTERED_STAGE
                && hasSpacedIndependentCorrectHistory(userId, reviewItem.getId(), reviewTime);

        reviewItem.setStage(decision.stage);
        reviewItem.setCurrentIntervalMinutes(decision.intervalMinutes);
        reviewItem.setLastFeedback(request.getFeedback());
        reviewItem.setMasteryScore(decision.masteryScore);
        reviewItem.setCorrectStreak(decision.correctStreak);
        reviewItem.setWrongStreak(decision.wrongStreak);
        reviewItem.setLapseCount(decision.lapseCount);
        reviewItem.setReviewCount(defaultValue(reviewItem.getReviewCount(), 0) + 1);
        reviewItem.setLastReviewTime(reviewTime);
        reviewItem.setNextReviewTime(nextReviewTime);
        reviewItem.setMasteredTime(mastered && reviewItem.getMasteredTime() == null
                ? reviewTime : reviewItem.getMasteredTime());
        reviewItem.setAlgorithmVersion(ReviewDefault.ALGORITHM_VERSION);
        reviewItem.setUpdateTime(reviewTime);
        // 使用版本号进行乐观锁更新，阻止多窗口并发反馈覆盖最新复习进度。
        if (reviewItemMapper.updateReviewFeedback(reviewItem, expectedVersion) != 1) {
            throw new LogicException(ErrorCodeConstants.REVIEW_FEEDBACK_CONFLICT);
        }

        // 复习任务达到掌握条件时，同步更新错题状态，保证错题列表与复习计划口径一致。
        if (mastered && !Integer.valueOf(2).equals(wrongQuestion.getStatus())) {
            boolean updated = wrongQuestionService.lambdaUpdate()
                    .eq(WrongQuestion::getId, wrongQuestion.getId())
                    .eq(WrongQuestion::getCreateId, userId)
                    .set(WrongQuestion::getStatus, 2)
                    .set(WrongQuestion::getUpdateTime, reviewTime)
                    .update();
            if (!updated) {
                throw new LogicException(ErrorCodeConstants.REVIEW_FEEDBACK_FAIL);
            }
        }

        // 最后落库本次反馈快照；事务内任一步失败都会回滚任务、错题和记录的变更。
        ReviewRecord reviewRecord = buildReviewRecord(request, reviewItem, wrongQuestion,
                userId, reviewTime, revealTime, scheduledTime, stageBefore, intervalBefore,
                masteryScoreBefore, decision, answerResult, independent, nextReviewTime);
        if (!reviewRecordService.save(reviewRecord)) {
            throw new LogicException(ErrorCodeConstants.REVIEW_FEEDBACK_FAIL);
        }
        wrongQuestionTimelineService.record(wrongQuestion.getId(), "REVIEW_FEEDBACK", "STUDENT",
                "已提交复习反馈：" + (answerResult.correct == 1 ? "作答正确" : "作答错误")
                        + (reviewRecord.getIsIndependent() == 1 ? "，计入独立作答" : "，不计入独立作答"), userId);
        if (mastered && !Integer.valueOf(2).equals(wrongQuestion.getStatus())) {
            wrongQuestionTimelineService.record(wrongQuestion.getId(), "STATUS_CHANGED", "SYSTEM",
                    "已满足独立作答与间隔复习规则，系统将状态变更为已掌握", userId);
        }
        // 任务version已递增，当前凭证会自然失效；相同requestId重试仍由幂等记录正常响应。
        return buildResponse(reviewRecord, reviewItem, mastered);
    }

    /**
     * 校验反馈类型及作答时间顺序，防止未查看答案便提交四级反馈。
     */
    private LocalDateTime validateAndGetRevealTime(SubmitReviewFeedbackReq request, Long userId,
                                                    ReviewItem reviewItem) {
        LocalDateTime now = LocalDateTime.now();
        Object ticketObject = redisService.get(
                ReviewCacheKey.answerRevealKey(userId, request.getReviewItemId()));
        if (ticketObject == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_NOT_REVEALED);
        }
        String[] ticketParts = String.valueOf(ticketObject).split(":", 3);
        if (ticketParts.length != 3
                || !request.getRevealToken().equals(ticketParts[0])
                || !String.valueOf(defaultValue(reviewItem.getVersion(), 0)).equals(ticketParts[1])) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_NOT_REVEALED);
        }
        LocalDateTime serverRevealTime;
        try {
            serverRevealTime = LocalDateTime.parse(ticketParts[2]);
        } catch (RuntimeException exception) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_NOT_REVEALED);
        }
        if (!ReviewFeedback.isValid(request.getFeedback())
                || serverRevealTime.isBefore(request.getStartTime())
                || serverRevealTime.isAfter(now.plusMinutes(1))) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_NOT_REVEALED);
        }
        return serverRevealTime;
    }

    /**
     * 根据四级反馈计算新阶段、复习间隔、连续正确次数和遗忘次数。
     */
    private ReviewDecision calculateDecision(Integer feedback, int stageBefore,
                                               int correctStreakBefore, int wrongStreakBefore,
                                               int lapseCountBefore, int masteryScoreBefore) {
        if (feedback == ReviewFeedback.FORGOT) {
            int score = clampMasteryScore(masteryScoreBefore - 20);
            return new ReviewDecision(ReviewStage.MIN, intervalMinutesByMastery(score),
                    score, 0, wrongStreakBefore + 1, lapseCountBefore + 1);
        }
        if (feedback == ReviewFeedback.DIFFICULT) {
            int stageAfter = Math.max(ReviewStage.MIN, stageBefore - 1);
            int score = clampMasteryScore(masteryScoreBefore - 10);
            return new ReviewDecision(stageAfter, intervalMinutesByMastery(score),
                    score, 0, wrongStreakBefore + 1, lapseCountBefore);
        }
        int stageStep = feedback == ReviewFeedback.EASY ? 2 : 1;
        int stageAfter = Math.min(stageBefore + stageStep, ReviewStage.MAX);
        int scoreStep = feedback == ReviewFeedback.EASY ? 20 : 15;
        int score = clampMasteryScore(masteryScoreBefore + scoreStep);
        return new ReviewDecision(stageAfter, intervalMinutesByMastery(score),
                score, correctStreakBefore + 1, 0, lapseCountBefore);
    }

    /**
     * 创建本次复习记录快照，保留调度前后数据供复盘和算法分析使用。
     */
    private ReviewRecord buildReviewRecord(SubmitReviewFeedbackReq request, ReviewItem reviewItem,
                                            WrongQuestion wrongQuestion, Long userId,
                                            LocalDateTime reviewTime, LocalDateTime revealTime,
                                            LocalDateTime scheduledTime, int stageBefore,
                                            int intervalBefore, int masteryScoreBefore,
                                            ReviewDecision decision,
                                            AnswerResult answerResult, boolean independent,
                                            LocalDateTime nextReviewTime) {
        ReviewRecord record = new ReviewRecord();
        record.setRequestId(request.getRequestId());
        record.setPlanId(reviewItem.getPlanId());
        record.setReviewItemId(reviewItem.getId());
        record.setUserId(userId);
        record.setWrongQuestionId(reviewItem.getWrongQuestionId());
        record.setQuestionTitleSnapshot(limitTitle(wrongQuestion.getQuestionTitle()));
        record.setSubject(wrongQuestion.getSubject() == null ? "" : wrongQuestion.getSubject());
        record.setSubjectNameSnapshot(wrongQuestion.getSubjectName() == null
                ? "" : wrongQuestion.getSubjectName());
        record.setScheduledTime(scheduledTime);
        record.setStartTime(request.getStartTime());
        record.setRevealTime(revealTime);
        record.setReviewTime(reviewTime);
        record.setFeedback(request.getFeedback());
        record.setAnswerDurationSeconds(calculateAnswerDuration(request, revealTime));
        record.setStudentAnswer(request.getStudentAnswer().trim());
        record.setIsCorrect(answerResult.correct);
        record.setIsIndependent(independent ? 1 : 0);
        record.setAnswerJudgeType(answerResult.judgeType);
        record.setIsOverdue(scheduledTime.isBefore(reviewTime.toLocalDate().atStartOfDay()) ? 1 : 0);
        record.setStageBefore(stageBefore);
        record.setStageAfter(decision.stage);
        record.setMasteryScoreBefore(masteryScoreBefore);
        record.setMasteryScoreAfter(decision.masteryScore);
        record.setMasteryScoreDelta(decision.masteryScore - masteryScoreBefore);
        record.setCorrectStreakAfter(decision.correctStreak);
        record.setWrongStreakAfter(decision.wrongStreak);
        record.setIntervalBeforeMinutes(intervalBefore);
        record.setIntervalAfterMinutes(decision.intervalMinutes);
        record.setNextReviewTime(nextReviewTime);
        record.setAlgorithmVersion(ReviewDefault.ALGORITHM_VERSION);
        record.setCreateTime(reviewTime);
        return record;
    }

    /**
     * 只有在答案曝光前已由服务端保存，且最终提交内容未被替换时，才计为独立作答。
     * 旧记录没有该字段，报告会保守地将其排除在独立正确指标之外。
     */
    private boolean hasIndependentAnswerProof(Long userId, Long reviewItemId, String studentAnswer,
                                               LocalDateTime revealTime) {
        Object draftObject = redisService.get(ReviewCacheKey.answerDraftKey(userId, reviewItemId));
        if (draftObject == null) {
            return false;
        }
        String[] parts = String.valueOf(draftObject).split(":", 2);
        if (parts.length != 2 || !SecureUtil.sha256(normalizeAnswer(studentAnswer)).equals(parts[0])) {
            return false;
        }
        try {
            return !LocalDateTime.parse(parts[1]).isAfter(revealTime);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** 最近两次也必须独立答对，且每次都在上次排定的时间之后完成。 */
    private boolean hasSpacedIndependentCorrectHistory(Long userId, Long reviewItemId,
                                                       LocalDateTime currentReviewTime) {
        List<ReviewRecord> history = reviewRecordService.list(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getUserId, userId)
                .eq(ReviewRecord::getReviewItemId, reviewItemId)
                .orderByDesc(ReviewRecord::getReviewTime)
                .last("LIMIT 2"));
        return ReviewMasteryEvidencePolicy.hasSpacedIndependentCorrectHistory(history, currentReviewTime);
    }

    /**
     * 可精确匹配的客观题由系统判定；其他题型必须由学生对照解析后自评。
     */
    private AnswerResult resolveAnswerResult(SubmitReviewFeedbackReq request,
                                             WrongQuestion wrongQuestion) {
        if (AUTO_JUDGE_QUESTION_TYPES.contains(wrongQuestion.getQuestionTypeName())
                && wrongQuestion.getCorrectAnswer() != null
                && !wrongQuestion.getCorrectAnswer().trim().isEmpty()) {
            boolean correct = normalizeAnswer(request.getStudentAnswer())
                    .equals(normalizeAnswer(wrongQuestion.getCorrectAnswer()));
            return new AnswerResult(correct ? 1 : 0, ReviewAnswerJudgeType.AUTO);
        }
        if (request.getSelfCorrect() == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_RESULT_REQUIRED);
        }
        return new AnswerResult(request.getSelfCorrect(), ReviewAnswerJudgeType.SELF);
    }

    /**
     * 自动判题只忽略 HTML、空白、常见分隔符和大小写，不做模糊语义判断，避免误判。
     */
    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.replaceAll("<[^>]+>", "")
                .replaceAll("[\\s，,。；;：:、]", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * 计算主动回忆耗时，并限制在整型字段可存储范围内。
     */
    private int calculateAnswerDuration(SubmitReviewFeedbackReq request, LocalDateTime revealTime) {
        long seconds = Math.max(0, Duration.between(request.getStartTime(), revealTime).getSeconds());
        return (int) Math.min(seconds, Integer.MAX_VALUE);
    }

    /**
     * 截断题目标题快照，避免超过数据库字段长度。
     */
    private String limitTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.length() > 255 ? title.substring(0, 255) : title;
    }

    /**
     * 根据已存在的复习记录构造幂等响应，不再次修改复习进度。
     */
    private SubmitReviewFeedbackResp buildIdempotentResponse(ReviewRecord record, Long userId) {
        ReviewItem reviewItem = reviewItemService.checkReviewItem(record.getReviewItemId(), userId);
        return buildResponse(record, reviewItem, reviewItem.getMasteredTime() != null);
    }

    /**
     * 组装前端展示所需的反馈结果和下一次复习安排。
     */
    private SubmitReviewFeedbackResp buildResponse(ReviewRecord record, ReviewItem reviewItem,
                                                    boolean mastered) {
        SubmitReviewFeedbackResp response = new SubmitReviewFeedbackResp();
        response.setReviewRecordId(record.getId());
        response.setFeedback(record.getFeedback());
        response.setStageAfter(record.getStageAfter());
        response.setMasteryScoreBefore(record.getMasteryScoreBefore());
        response.setMasteryScoreAfter(record.getMasteryScoreAfter());
        response.setMasteryScoreDelta(record.getMasteryScoreDelta());
        response.setIntervalAfterMinutes(record.getIntervalAfterMinutes());
        response.setCorrectStreak(reviewItem.getCorrectStreak());
        response.setWrongStreak(reviewItem.getWrongStreak());
        response.setNextReviewTime(record.getNextReviewTime());
        response.setNextReviewReason(ReviewPlanExplanationPolicy.nextReviewReason(record));
        response.setMastered(mastered);
        return response;
    }

    /**
     * 将旧版阶段映射为可展示的掌握度，兼容还没有 mastery_score 的历史复习项。
     */
    private int stageToMasteryScore(int stage) {
        return clampMasteryScore(DEFAULT_MASTERY_SCORE + Math.max(0, stage - ReviewStage.INITIAL) * 8);
    }

    /**
     * 根据掌握度决定下次复习间隔：低分高频，高分低频。
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
     * 掌握度统一限制在 0-100。
     */
    private int clampMasteryScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 兼容历史数据中的空值，统一回退到算法默认值。
     */
    private int defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 四级反馈算法的单次计算结果。
     */
    private static class ReviewDecision {

        private final int stage;

        private final int intervalMinutes;

        private final int masteryScore;

        private final int correctStreak;

        private final int wrongStreak;

        private final int lapseCount;

        private ReviewDecision(int stage, int intervalMinutes, int masteryScore,
                               int correctStreak, int wrongStreak, int lapseCount) {
            this.stage = stage;
            this.intervalMinutes = intervalMinutes;
            this.masteryScore = masteryScore;
            this.correctStreak = correctStreak;
            this.wrongStreak = wrongStreak;
            this.lapseCount = lapseCount;
        }
    }

    /**
     * 单次答案判定结果。
     */
    private static class AnswerResult {

        private final int correct;
        private final int judgeType;

        private AnswerResult(int correct, int judgeType) {
            this.correct = correct;
            this.judgeType = judgeType;
        }
    }
}
