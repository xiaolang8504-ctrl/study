package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.review.dto.request.ReviewLearningReportReq;
import com.study.module.system.review.dto.response.ReviewReportActionDetailResp;
import com.study.module.system.review.dto.response.ReviewReportActionQuestionResp;
import com.study.module.system.review.dto.response.ReviewLearningReportResp;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewMetricPolicy;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.review.service.ReviewLearningReportService;
import com.study.module.system.review.service.LearningMetricDailyService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.constants.WrongQuestionDiagnosisPolicy;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisStatisticsReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointStatisticsReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionAbilityLevelStatisticsResp;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学情报告服务实现。
 */
@Service
public class ReviewLearningReportServiceImpl implements ReviewLearningReportService {

    private static final int ACTION_QUESTION_LIMIT = 20;
    private static final String ACTION_KNOWLEDGE_POINT = "KNOWLEDGE_POINT";
    private static final String ACTION_ERROR_LABEL = "ERROR_LABEL";
    private static final String ACTION_LOW_RETENTION = "LOW_RETENTION";
    private static final String UNMARKED_ERROR_LABEL = "未标记";

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

    @Autowired
    private WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

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
        response.setAbilityLevelStatisticsList(buildAbilityLevelStatistics(wrongQuestions));
        response.setDailyMetricList(learningMetricDailyService.learningMetricTrend(userId, subject,
                LocalDate.now().minusDays(29), LocalDate.now()));
        List<Long> questionIds = wrongQuestions.stream().map(WrongQuestion::getId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toList());
        response.setOverdueReviewCount(questionIds.isEmpty() ? 0 : (int) reviewItemService.count(
                new LambdaQueryWrapper<ReviewItem>().eq(ReviewItem::getUserId, userId)
                        .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                        .in(ReviewItem::getWrongQuestionId, questionIds)
                        .lt(ReviewItem::getNextReviewTime, LocalDate.now().atStartOfDay())));
        response.setActionDetail(buildActionDetail(request, userId, wrongQuestions, reviewRecords));
        return response;
    }

    /**
     * 将报告中的薄弱指标转换为当前学生可直接加入组卷篮的错题。仍使用报告接口和同一权限，
     * 且所有候选题均从已按当前用户、科目过滤过的数据中再次筛选。
     */
    private ReviewReportActionDetailResp buildActionDetail(ReviewLearningReportReq request, Long userId,
                                                            List<WrongQuestion> wrongQuestions,
                                                            List<ReviewRecord> reviewRecords) {
        if (!StringUtils.hasText(request.getActionType())) {
            return null;
        }
        List<WrongQuestion> candidateQuestions = actionCandidates(request, wrongQuestions, reviewRecords);
        Map<Long, ReviewQuestionRecordSummary> recordSummaryMap = buildRecordSummaryMap(reviewRecords);
        Map<Long, ReviewItem> reviewItemMap = loadReviewItems(userId, candidateQuestions);
        sortActionCandidates(candidateQuestions, request.getActionType(), recordSummaryMap, reviewItemMap);

        ReviewReportActionDetailResp response = buildActionDetailHeader(request, candidateQuestions.size());
        List<ReviewReportActionQuestionResp> questionList = candidateQuestions.stream()
                .limit(ACTION_QUESTION_LIMIT)
                .map(question -> buildActionQuestion(question, request.getActionType(), recordSummaryMap,
                        reviewItemMap))
                .collect(Collectors.toList());
        response.setQuestionList(questionList);
        return response;
    }

    private List<WrongQuestion> actionCandidates(ReviewLearningReportReq request,
                                                  List<WrongQuestion> wrongQuestions,
                                                  List<ReviewRecord> reviewRecords) {
        List<WrongQuestion> availableQuestions = wrongQuestions.stream()
                .filter(question -> question.getId() != null)
                .filter(question -> question.getMergedToId() == null)
                .collect(Collectors.toList());
        if (ACTION_KNOWLEDGE_POINT.equals(request.getActionType())) {
            if (request.getKnowledgePointId() == null) {
                return Collections.emptyList();
            }
            Set<Long> wrongQuestionIds = wrongQuestionKnowledgePointService.list(
                    new LambdaQueryWrapper<WrongQuestionKnowledgePoint>()
                            .eq(WrongQuestionKnowledgePoint::getKnowledgePointId, request.getKnowledgePointId()))
                    .stream().map(WrongQuestionKnowledgePoint::getWrongQuestionId)
                    .collect(Collectors.toSet());
            return availableQuestions.stream().filter(question -> wrongQuestionIds.contains(question.getId()))
                    .collect(Collectors.toList());
        }
        if (ACTION_ERROR_LABEL.equals(request.getActionType())) {
            if (!StringUtils.hasText(request.getErrorLabel())) {
                return Collections.emptyList();
            }
            return availableQuestions.stream()
                    .filter(question -> parseErrorLabels(question.getErrorLabels()).contains(request.getErrorLabel()))
                    .collect(Collectors.toList());
        }
        if (ACTION_LOW_RETENTION.equals(request.getActionType())) {
            Set<Long> reviewedQuestionIds = reviewRecords.stream()
                    .filter(ReviewMetricPolicy::isEffectiveReview)
                    .map(ReviewRecord::getWrongQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            return availableQuestions.stream().filter(question -> reviewedQuestionIds.contains(question.getId()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private ReviewReportActionDetailResp buildActionDetailHeader(ReviewLearningReportReq request,
                                                                   int questionCount) {
        ReviewReportActionDetailResp response = new ReviewReportActionDetailResp();
        response.setActionType(request.getActionType());
        response.setQuestionCount(questionCount);
        if (ACTION_KNOWLEDGE_POINT.equals(request.getActionType())) {
            response.setTitle("知识点薄弱题");
            response.setDescription(request.getKnowledgePointId() == null ? "请选择一个知识点后查看对应错题。"
                    : "已找出关联该知识点的错题；可勾选后加入组卷篮重练。");
        } else if (ACTION_ERROR_LABEL.equals(request.getActionType())) {
            response.setTitle("错因专项题");
            response.setDescription(StringUtils.hasText(request.getErrorLabel())
                    ? "已按错因“" + request.getErrorLabel() + "”筛出错题；可勾选后集中练习。"
                    : "请选择一个错因后查看对应错题。");
        } else {
            response.setTitle("保持率薄弱题");
            response.setDescription(questionCount == 0
                    ? "近30天暂无带独立作答凭证的错题，完成复习后会出现可重练题目。"
                    : "按近30天独立作答正确率从低到高排序；优先重练排在前面的题目。");
        }
        return response;
    }

    private Map<Long, ReviewQuestionRecordSummary> buildRecordSummaryMap(List<ReviewRecord> reviewRecords) {
        Map<Long, ReviewQuestionRecordSummary> summaryMap = new HashMap<>();
        for (ReviewRecord record : reviewRecords) {
            if (!ReviewMetricPolicy.isEffectiveReview(record) || record.getWrongQuestionId() == null) {
                continue;
            }
            ReviewQuestionRecordSummary summary = summaryMap.computeIfAbsent(record.getWrongQuestionId(),
                    key -> new ReviewQuestionRecordSummary());
            summary.independentReviewCount++;
            if (ReviewMetricPolicy.isIndependentCorrect(record)) {
                summary.independentCorrectCount++;
            }
            if (record.getReviewTime() != null && (summary.lastReviewTime == null
                    || record.getReviewTime().isAfter(summary.lastReviewTime))) {
                summary.lastReviewTime = record.getReviewTime();
            }
        }
        return summaryMap;
    }

    private Map<Long, ReviewItem> loadReviewItems(Long userId, List<WrongQuestion> wrongQuestions) {
        List<Long> wrongQuestionIds = wrongQuestions.stream().map(WrongQuestion::getId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toList());
        if (wrongQuestionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewItemService.list(new LambdaQueryWrapper<ReviewItem>()
                        .eq(ReviewItem::getUserId, userId)
                        .in(ReviewItem::getWrongQuestionId, wrongQuestionIds))
                .stream().collect(Collectors.toMap(ReviewItem::getWrongQuestionId, item -> item,
                        this::newerReviewItem));
    }

    private ReviewItem newerReviewItem(ReviewItem left, ReviewItem right) {
        if (left.getLastReviewTime() == null) {
            return right;
        }
        if (right.getLastReviewTime() == null) {
            return left;
        }
        return left.getLastReviewTime().isAfter(right.getLastReviewTime()) ? left : right;
    }

    private void sortActionCandidates(List<WrongQuestion> questions, String actionType,
                                      Map<Long, ReviewQuestionRecordSummary> recordSummaryMap,
                                      Map<Long, ReviewItem> reviewItemMap) {
        if (ACTION_LOW_RETENTION.equals(actionType)) {
            questions.sort(Comparator.comparingInt((WrongQuestion question) ->
                            retentionRate(recordSummaryMap.get(question.getId())))
                    .thenComparing((WrongQuestion question) -> reviewCount(recordSummaryMap.get(question.getId())),
                            Comparator.reverseOrder())
                    .thenComparing((WrongQuestion question) -> lastReviewTime(recordSummaryMap.get(question.getId())),
                            Comparator.nullsLast(Comparator.reverseOrder())));
            return;
        }
        questions.sort(Comparator.comparingInt((WrongQuestion question) -> actionStatusRank(question.getStatus()))
                .thenComparing(question -> masteryScore(reviewItemMap.get(question.getId())))
                .thenComparing(WrongQuestion::getLevel, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(WrongQuestion::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private ReviewReportActionQuestionResp buildActionQuestion(WrongQuestion question, String actionType,
                                                                Map<Long, ReviewQuestionRecordSummary> recordSummaryMap,
                                                                Map<Long, ReviewItem> reviewItemMap) {
        ReviewQuestionRecordSummary recordSummary = recordSummaryMap.get(question.getId());
        ReviewItem reviewItem = reviewItemMap.get(question.getId());
        ReviewReportActionQuestionResp response = new ReviewReportActionQuestionResp();
        response.setWrongQuestionId(question.getId());
        response.setQuestionTitle(question.getQuestionTitle());
        response.setSubject(question.getSubject());
        response.setSubjectName(question.getSubjectName());
        response.setLearningPoint(question.getLearningPoint());
        response.setErrorLabels(question.getErrorLabels());
        response.setStatus(question.getStatus());
        response.setLevel(question.getLevel());
        response.setLastReviewTime(lastReviewTime(recordSummary));
        response.setIndependentReviewCount30Days(reviewCount(recordSummary));
        response.setIndependentCorrectCount30Days(correctCount(recordSummary));
        response.setReviewCount(reviewItem == null ? 0 : defaultValue(reviewItem.getReviewCount()));
        response.setLapseCount(reviewItem == null ? 0 : defaultValue(reviewItem.getLapseCount()));
        response.setMasteryScore(reviewItem == null ? null : reviewItem.getMasteryScore());
        response.setSelectedReason(buildSelectedReason(question, actionType, recordSummary, reviewItem));
        return response;
    }

    private String buildSelectedReason(WrongQuestion question, String actionType,
                                       ReviewQuestionRecordSummary recordSummary, ReviewItem reviewItem) {
        if (ACTION_ERROR_LABEL.equals(actionType)) {
            return "已标注该错因，当前为" + statusText(question.getStatus()) + "。";
        }
        if (ACTION_LOW_RETENTION.equals(actionType)) {
            return "近30天独立正确" + correctCount(recordSummary) + "/" + reviewCount(recordSummary)
                    + "（" + retentionRate(recordSummary) + "%）"
                    + (reviewItem != null && reviewItem.getLapseCount() != null && reviewItem.getLapseCount() > 0
                    ? "，累计遗忘" + reviewItem.getLapseCount() + "次" : "");
        }
        return "关联该知识点，当前为" + statusText(question.getStatus()) + "。";
    }

    private List<String> parseErrorLabels(String errorLabels) {
        if (!StringUtils.hasText(errorLabels)) {
            return Collections.singletonList(UNMARKED_ERROR_LABEL);
        }
        return java.util.Arrays.stream(errorLabels.split("[,，]"))
                .map(String::trim).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
    }

    private int actionStatusRank(Integer status) {
        return status == null ? Integer.MAX_VALUE : status;
    }

    private String statusText(Integer status) {
        if (Integer.valueOf(WrongQuestionStatus.PENDING_CORRECTION).equals(status)) {
            return "待订正";
        }
        if (Integer.valueOf(WrongQuestionStatus.CORRECTED).equals(status)) {
            return "复习中";
        }
        if (Integer.valueOf(WrongQuestionStatus.MASTERED).equals(status)) {
            return "已掌握";
        }
        if (Integer.valueOf(WrongQuestionStatus.ARCHIVED).equals(status)) {
            return "已归档";
        }
        return "未设置状态";
    }

    private int retentionRate(ReviewQuestionRecordSummary summary) {
        return ReviewMetricPolicy.percent(correctCount(summary), reviewCount(summary));
    }

    private int reviewCount(ReviewQuestionRecordSummary summary) {
        return summary == null ? 0 : summary.independentReviewCount;
    }

    private int correctCount(ReviewQuestionRecordSummary summary) {
        return summary == null ? 0 : summary.independentCorrectCount;
    }

    private LocalDateTime lastReviewTime(ReviewQuestionRecordSummary summary) {
        return summary == null ? null : summary.lastReviewTime;
    }

    private int masteryScore(ReviewItem reviewItem) {
        return reviewItem == null || reviewItem.getMasteryScore() == null ? 0 : reviewItem.getMasteryScore();
    }

    private static class ReviewQuestionRecordSummary {
        private int independentReviewCount;
        private int independentCorrectCount;
        private LocalDateTime lastReviewTime;
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

    /**
     * 按能力层级汇总当前报告已筛选出的个人错题；历史未标注题单独保留，避免被误判为基础题。
     */
    private List<WrongQuestionAbilityLevelStatisticsResp> buildAbilityLevelStatistics(
            List<WrongQuestion> wrongQuestions) {
        Map<String, WrongQuestionAbilityLevelStatisticsResp> statisticsMap = new LinkedHashMap<>();
        WrongQuestionDiagnosisPolicy.abilityLabels().forEach((level, name) -> {
            WrongQuestionAbilityLevelStatisticsResp item = new WrongQuestionAbilityLevelStatisticsResp();
            item.setAbilityLevel(level);
            item.setAbilityLevelName(name);
            item.setWrongQuestionCount(0);
            item.setPendingCorrectionCount(0);
            item.setMasteredCount(0);
            statisticsMap.put(level, item);
        });
        for (WrongQuestion question : wrongQuestions) {
            String abilityLevel = WrongQuestionDiagnosisPolicy.isAbilityLevel(question.getAbilityLevel())
                    && StringUtils.hasText(question.getAbilityLevel()) ? question.getAbilityLevel() : "UNMARKED";
            WrongQuestionAbilityLevelStatisticsResp item = statisticsMap.computeIfAbsent(abilityLevel, key -> {
                WrongQuestionAbilityLevelStatisticsResp unmarked = new WrongQuestionAbilityLevelStatisticsResp();
                unmarked.setAbilityLevel("UNMARKED");
                unmarked.setAbilityLevelName("未标注");
                unmarked.setWrongQuestionCount(0);
                unmarked.setPendingCorrectionCount(0);
                unmarked.setMasteredCount(0);
                return unmarked;
            });
            item.setWrongQuestionCount(item.getWrongQuestionCount() + 1);
            if (Integer.valueOf(WrongQuestionStatus.PENDING_CORRECTION).equals(question.getStatus())) {
                item.setPendingCorrectionCount(item.getPendingCorrectionCount() + 1);
            }
            if (Integer.valueOf(WrongQuestionStatus.MASTERED).equals(question.getStatus())) {
                item.setMasteredCount(item.getMasteredCount() + 1);
            }
        }
        return statisticsMap.values().stream()
                .filter(item -> item.getWrongQuestionCount() > 0)
                .sorted(Comparator.comparing(WrongQuestionAbilityLevelStatisticsResp::getWrongQuestionCount)
                        .reversed())
                .collect(Collectors.toList());
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
