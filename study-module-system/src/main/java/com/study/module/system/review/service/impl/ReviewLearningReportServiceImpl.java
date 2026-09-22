package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.review.dto.request.ReviewLearningReportReq;
import com.study.module.system.review.dto.response.ReviewLearningReportResp;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewMetricPolicy;
import com.study.module.system.review.service.ReviewLearningReportService;
import com.study.module.system.review.service.LearningMetricDailyService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisStatisticsReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointStatisticsReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionErrorAnalysisService;
import com.study.module.system.wrongquestion.service.WrongQuestionKnowledgePointBindService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学情报告服务实现。
 */
@Service
public class ReviewLearningReportServiceImpl implements ReviewLearningReportService {

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private ReviewRecordService reviewRecordService;

    @Autowired
    private WrongQuestionKnowledgePointBindService wrongQuestionKnowledgePointBindService;

    @Autowired
    private WrongQuestionErrorAnalysisService wrongQuestionErrorAnalysisService;

    @Autowired
    private LearningMetricDailyService learningMetricDailyService;

    @Autowired
    private ReviewItemService reviewItemService;

    /**
     * 汇总当前学生的错题、复习和薄弱点数据。
     */
    @Override
    public ReviewLearningReportResp reviewLearningReport(ReviewLearningReportReq request) {
        Long userId = AccountUtils.getUserId();
        String subject = request.getSubject();
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getCreateId, userId)
                .eq(StringUtils.hasText(subject), WrongQuestion::getSubject, subject));

        ReviewLearningReportResp response = new ReviewLearningReportResp();
        response.setSubject(subject);
        response.setWrongQuestionCount(wrongQuestions.size());
        response.setPendingCorrectionCount(countWrongQuestions(wrongQuestions, WrongQuestionStatus.PENDING_CORRECTION));
        response.setCorrectedCount(countWrongQuestions(wrongQuestions, WrongQuestionStatus.CORRECTED));
        response.setMasteredCount(countWrongQuestions(wrongQuestions, WrongQuestionStatus.MASTERED));
        response.setArchivedCount(countWrongQuestions(wrongQuestions, WrongQuestionStatus.ARCHIVED));
        response.setMasteryRate(rate(response.getMasteredCount(), response.getWrongQuestionCount()));

        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        List<ReviewRecord> reviewRecords = reviewRecordService.list(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getUserId, userId)
                .eq(StringUtils.hasText(subject), ReviewRecord::getSubject, subject)
                .ge(ReviewRecord::getReviewTime, startTime));
        response.setReviewCount30Days((int) reviewRecords.stream()
                .filter(ReviewMetricPolicy::isEffectiveReview).count());
        response.setNewWrongQuestionCount30Days((int) wrongQuestions.stream()
                .filter(item -> item.getCreateTime() != null && !item.getCreateTime().isBefore(startTime)).count());
        response.setDueReviewCompletedCount30Days((int) reviewRecords.stream()
                .filter(ReviewMetricPolicy::isEffectiveReview)
                .filter(item -> item.getScheduledTime() != null && item.getReviewTime() != null
                        && !item.getScheduledTime().isAfter(item.getReviewTime())).count());
        List<ReviewRecord> independentRecords = reviewRecords.stream()
                .filter(ReviewMetricPolicy::isEffectiveReview)
                .collect(Collectors.toList());
        long judgedCount = independentRecords.size();
        long correctCount = independentRecords.stream().filter(ReviewMetricPolicy::isIndependentCorrect).count();
        response.setIndependentCorrectCount30Days((int) correctCount);
        response.setRetentionSampleCount30Days((int) judgedCount);
        response.setRetentionRateReliable(judgedCount >= 3);
        response.setRetentionRate30Days(ReviewMetricPolicy.percent(correctCount, judgedCount));
        response.setWeakLearningPointList(buildWeakLearningPoints(subject));
        response.setErrorAnalysisList(buildErrorAnalysis(subject));
        response.setDailyMetricList(learningMetricDailyService.learningMetricTrend(userId, subject,
                LocalDate.now().minusDays(29), LocalDate.now()));
        List<Long> questionIds = wrongQuestions.stream().map(WrongQuestion::getId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toList());
        response.setOverdueReviewCount(questionIds.isEmpty() ? 0 : (int) reviewItemService.count(
                new LambdaQueryWrapper<ReviewItem>().eq(ReviewItem::getUserId, userId)
                        .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                        .in(ReviewItem::getWrongQuestionId, questionIds)
                        .lt(ReviewItem::getNextReviewTime, LocalDate.now().atStartOfDay())));
        return response;
    }

    /**
     * 按掌握率从低到高、错题数从多到少排序，确定优先练习的知识点。
     */
    private List<WrongQuestionKnowledgePointStatisticsResp> buildWeakLearningPoints(String subject) {
        WrongQuestionKnowledgePointStatisticsReq request = new WrongQuestionKnowledgePointStatisticsReq();
        request.setSubject(subject);
        return wrongQuestionKnowledgePointBindService.wrongQuestionKnowledgePointStatistics(request).stream()
                .sorted(Comparator.comparing(this::masteryRate)
                        .thenComparing(WrongQuestionKnowledgePointStatisticsResp::getWrongQuestionCount,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<com.study.module.system.wrongquestion.dto.response.WrongQuestionErrorAnalysisStatisticsResp>
    buildErrorAnalysis(String subject) {
        WrongQuestionErrorAnalysisStatisticsReq request = new WrongQuestionErrorAnalysisStatisticsReq();
        request.setSubject(subject);
        return wrongQuestionErrorAnalysisService.wrongQuestionErrorAnalysisStatistics(request);
    }

    private int countWrongQuestions(List<WrongQuestion> wrongQuestions, int status) {
        return (int) wrongQuestions.stream().filter(item -> Integer.valueOf(status).equals(item.getStatus())).count();
    }

    private int masteryRate(WrongQuestionKnowledgePointStatisticsResp item) {
        return rate(defaultValue(item.getMasteredCount()), defaultValue(item.getWrongQuestionCount()));
    }

    private int rate(int numerator, int denominator) {
        return denominator == 0 ? 0 : (int) Math.round(numerator * 100.0 / denominator);
    }

    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}
