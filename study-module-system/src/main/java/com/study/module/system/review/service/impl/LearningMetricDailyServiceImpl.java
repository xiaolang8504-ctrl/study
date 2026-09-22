package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.constants.ReviewMetricPolicy;
import com.study.module.system.review.dto.response.LearningMetricDailyResp;
import com.study.module.system.review.entity.LearningMetricDaily;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.mapper.LearningMetricDailyMapper;
import com.study.module.system.review.service.LearningMetricDailyService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学习日快照服务实现。
 */
@Service
public class LearningMetricDailyServiceImpl extends ServiceImpl<LearningMetricDailyMapper, LearningMetricDaily>
        implements LearningMetricDailyService {

    private static final String ALL_SUBJECT = "ALL";

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private ReviewRecordService reviewRecordService;

    /**
     * 每日生成前一天快照。部署多实例时应只启用一个系统服务实例执行该定时任务。
     */
    @Scheduled(cron = "${review.learning-metric.snapshot-cron:0 10 0 * * ?}")
    public void buildYesterdaySnapshot() {
        buildDailySnapshot(LocalDate.now().minusDays(1));
    }

    /**
     * 以当天结束时的错题状态和当天完成的复习记录生成可重跑快照。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buildDailySnapshot(LocalDate metricDate) {
        LocalDateTime startTime = metricDate.atStartOfDay();
        LocalDateTime endTime = metricDate.plusDays(1).atStartOfDay();
        Map<MetricKey, MetricAccumulator> metricMap = new HashMap<>();
        for (WrongQuestion question : wrongQuestionService.list()) {
            if (question.getCreateId() == null) {
                continue;
            }
            addQuestion(metricMap, question, normalizeSubject(question.getSubject()));
            addQuestion(metricMap, question, ALL_SUBJECT);
        }
        List<ReviewRecord> reviewRecordList = reviewRecordService.list(new LambdaQueryWrapper<ReviewRecord>()
                .ge(ReviewRecord::getReviewTime, startTime)
                .lt(ReviewRecord::getReviewTime, endTime));
        for (ReviewRecord reviewRecord : reviewRecordList) {
            if (reviewRecord.getUserId() == null) {
                continue;
            }
            addReviewRecord(metricMap, reviewRecord, normalizeSubject(reviewRecord.getSubject()));
            addReviewRecord(metricMap, reviewRecord, ALL_SUBJECT);
        }
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<MetricKey, MetricAccumulator> entry : metricMap.entrySet()) {
            upsertMetric(metricDate, entry.getKey(), entry.getValue(), now);
        }
    }

    @Override
    public List<LearningMetricDailyResp> learningMetricTrend(Long userId, String subject,
                                                              LocalDate startDate, LocalDate endDate) {
        String metricSubject = StringUtils.hasText(subject) ? subject : ALL_SUBJECT;
        return list(new LambdaQueryWrapper<LearningMetricDaily>()
                .eq(LearningMetricDaily::getUserId, userId)
                .eq(LearningMetricDaily::getSubject, metricSubject)
                .ge(LearningMetricDaily::getMetricDate, startDate)
                .le(LearningMetricDaily::getMetricDate, endDate)
                .orderByAsc(LearningMetricDaily::getMetricDate))
                .stream().map(item -> {
                    LearningMetricDailyResp response = new LearningMetricDailyResp();
                    BeanUtils.copyProperties(item, response);
                    return response;
                }).collect(Collectors.toList());
    }

    private void addQuestion(Map<MetricKey, MetricAccumulator> metricMap, WrongQuestion question, String subject) {
        MetricAccumulator accumulator = metricMap.computeIfAbsent(new MetricKey(question.getCreateId(), subject),
                item -> new MetricAccumulator());
        accumulator.wrongQuestionCount++;
        if (Objects.equals(question.getStatus(), WrongQuestionStatus.PENDING_CORRECTION)) {
            accumulator.pendingCorrectionCount++;
        } else if (Objects.equals(question.getStatus(), WrongQuestionStatus.CORRECTED)) {
            accumulator.correctedCount++;
        } else if (Objects.equals(question.getStatus(), WrongQuestionStatus.MASTERED)) {
            accumulator.masteredCount++;
        } else if (Objects.equals(question.getStatus(), WrongQuestionStatus.ARCHIVED)) {
            accumulator.archivedCount++;
        }
    }

    private void addReviewRecord(Map<MetricKey, MetricAccumulator> metricMap, ReviewRecord reviewRecord, String subject) {
        MetricAccumulator accumulator = metricMap.computeIfAbsent(new MetricKey(reviewRecord.getUserId(), subject),
                item -> new MetricAccumulator());
        if (ReviewMetricPolicy.isEffectiveReview(reviewRecord)) {
            accumulator.reviewCount++;
            accumulator.judgedAnswerCount++;
            if (ReviewMetricPolicy.isIndependentCorrect(reviewRecord)) {
                accumulator.correctAnswerCount++;
            }
        }
    }

    private void upsertMetric(LocalDate metricDate, MetricKey key, MetricAccumulator accumulator, LocalDateTime now) {
        LearningMetricDaily metric = getOne(new LambdaQueryWrapper<LearningMetricDaily>()
                .eq(LearningMetricDaily::getMetricDate, metricDate)
                .eq(LearningMetricDaily::getUserId, key.userId)
                .eq(LearningMetricDaily::getSubject, key.subject));
        if (metric == null) {
            metric = new LearningMetricDaily();
            metric.setMetricDate(metricDate);
            metric.setUserId(key.userId);
            metric.setSubject(key.subject);
            metric.setCreateTime(now);
        }
        metric.setWrongQuestionCount(accumulator.wrongQuestionCount);
        metric.setPendingCorrectionCount(accumulator.pendingCorrectionCount);
        metric.setCorrectedCount(accumulator.correctedCount);
        metric.setMasteredCount(accumulator.masteredCount);
        metric.setArchivedCount(accumulator.archivedCount);
        metric.setMasteryRate(rate(accumulator.masteredCount, accumulator.wrongQuestionCount));
        metric.setReviewCount(accumulator.reviewCount);
        metric.setJudgedAnswerCount(accumulator.judgedAnswerCount);
        metric.setCorrectAnswerCount(accumulator.correctAnswerCount);
        metric.setRetentionRate(rate(accumulator.correctAnswerCount, accumulator.judgedAnswerCount));
        metric.setUpdateTime(now);
        saveOrUpdate(metric);
    }

    private int rate(int numerator, int denominator) {
        return denominator == 0 ? 0 : (int) Math.round(numerator * 100.0 / denominator);
    }

    private String normalizeSubject(String subject) {
        return StringUtils.hasText(subject) ? subject : "UNKNOWN";
    }

    private static final class MetricKey {
        private final Long userId;
        private final String subject;

        private MetricKey(Long userId, String subject) {
            this.userId = userId;
            this.subject = subject;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof MetricKey)) {
                return false;
            }
            MetricKey that = (MetricKey) object;
            return Objects.equals(userId, that.userId) && Objects.equals(subject, that.subject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, subject);
        }
    }

    private static final class MetricAccumulator {
        private int wrongQuestionCount;
        private int pendingCorrectionCount;
        private int correctedCount;
        private int masteredCount;
        private int archivedCount;
        private int reviewCount;
        private int judgedAnswerCount;
        private int correctAnswerCount;
    }
}
