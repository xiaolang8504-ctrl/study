package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.review.dto.request.PracticeSessionPageListReq;
import com.study.module.system.review.dto.response.PracticeStatisticsItemResp;
import com.study.module.system.review.dto.response.PracticeStatisticsResp;
import com.study.module.system.review.dto.response.PracticeTrendResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.PracticeStatisticsService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专项练习统计服务实现
 */
@Service
public class PracticeStatisticsServiceImpl implements PracticeStatisticsService {

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    PracticeSessionQuestionService practiceSessionQuestionService;

    /**
     * 查询练习统计数据。
     */
    @Override
    public PracticeStatisticsResp practiceStatistics(PracticeSessionPageListReq request) {
        Long userId = AccountUtils.getUserId();
        List<PracticeSession> sessions = practiceSessionService.list(buildSessionWrapper(userId, request));
        PracticeStatisticsResp response = new PracticeStatisticsResp();
        response.setSessionCount(sessions.size());
        response.setFinishedSessionCount((int) sessions.stream()
                .filter(session -> Integer.valueOf(1).equals(session.getStatus()))
                .count());
        response.setQuestionCount(sessions.stream().mapToInt(item -> defaultValue(item.getQuestionCount())).sum());
        response.setAnsweredCount(sessions.stream().mapToInt(item -> defaultValue(item.getAnsweredCount())).sum());
        response.setCorrectCount(sessions.stream().mapToInt(item -> defaultValue(item.getCorrectCount())).sum());
        response.setWrongCount(sessions.stream().mapToInt(item -> defaultValue(item.getWrongCount())).sum());
        response.setTotalDurationSeconds(sessions.stream()
                .mapToInt(item -> defaultValue(item.getTotalDurationSeconds())).sum());
        response.setAverageAccuracyRate(response.getAnsweredCount() == 0 ? 0
                : (int) Math.round(response.getCorrectCount() * 100.0 / response.getAnsweredCount()));
        List<Long> sessionIds = sessions.stream().map(PracticeSession::getId).collect(Collectors.toList());
        if (sessionIds.isEmpty()) {
            response.setWeakLearningPointList(java.util.Collections.emptyList());
            response.setErrorLabelList(java.util.Collections.emptyList());
            response.setTrendList(java.util.Collections.emptyList());
            return response;
        }
        List<PracticeSessionQuestion> questions = practiceSessionQuestionService.lambdaQuery()
                .in(PracticeSessionQuestion::getSessionId, sessionIds)
                .eq(PracticeSessionQuestion::getUserId, userId)
                .isNotNull(PracticeSessionQuestion::getAnswerTime)
                .list();
        response.setWeakLearningPointList(groupQuestionStatistics(questions));
        response.setErrorLabelList(groupSessionStatistics(sessions));
        response.setTrendList(Arrays.asList(buildTrend(sessions, questions, 7),
                buildTrend(sessions, questions, 30)));
        return response;
    }

    private LambdaQueryWrapper<PracticeSession> buildSessionWrapper(Long userId,
                                                                    PracticeSessionPageListReq request) {
        LambdaQueryWrapper<PracticeSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSession::getUserId, userId);
        wrapper.eq(StringUtils.hasText(request.getPracticeType()), PracticeSession::getPracticeType,
                request.getPracticeType());
        wrapper.eq(StringUtils.hasText(request.getSubject()), PracticeSession::getSubject,
                request.getSubject());
        wrapper.and(StringUtils.hasText(request.getKeyWord()), query -> query
                .like(PracticeSession::getTitle, request.getKeyWord())
                .or().like(PracticeSession::getLearningPoint, request.getKeyWord())
                .or().like(PracticeSession::getErrorLabel, request.getKeyWord()));
        return wrapper;
    }

    /**
     * 处理业务数据。
     */
    private List<PracticeStatisticsItemResp> groupQuestionStatistics(List<PracticeSessionQuestion> questions) {
        Map<String, List<PracticeSessionQuestion>> groupMap = questions.stream()
                .filter(item -> StringUtils.hasText(item.getLearningPoint()))
                .collect(Collectors.groupingBy(PracticeSessionQuestion::getLearningPoint));
        return groupMap.entrySet().stream()
                .map(entry -> buildQuestionItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PracticeStatisticsItemResp::getAccuracyRate)
                        .thenComparing(PracticeStatisticsItemResp::getWrongCount, Comparator.reverseOrder()))
                .limit(6)
                .collect(Collectors.toList());
    }

    /**
     * 处理业务数据。
     */
    private List<PracticeStatisticsItemResp> groupSessionStatistics(List<PracticeSession> sessions) {
        Map<String, List<PracticeSession>> groupMap = sessions.stream()
                .filter(item -> StringUtils.hasText(item.getErrorLabel()))
                .collect(Collectors.groupingBy(PracticeSession::getErrorLabel));
        return groupMap.entrySet().stream()
                .map(entry -> buildSessionItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PracticeStatisticsItemResp::getWrongCount).reversed())
                .limit(6)
                .collect(Collectors.toList());
    }

    /**
     * 构建业务处理结果。
     */
    private PracticeStatisticsItemResp buildQuestionItem(String name, List<PracticeSessionQuestion> questions) {
        int questionCount = questions.size();
        int correctCount = (int) questions.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsCorrect()))
                .count();
        return buildItem(name, questionCount, correctCount);
    }

    /**
     * 构建业务处理结果。
     */
    private PracticeStatisticsItemResp buildSessionItem(String name, List<PracticeSession> sessions) {
        int questionCount = sessions.stream().mapToInt(item -> defaultValue(item.getAnsweredCount())).sum();
        int correctCount = sessions.stream().mapToInt(item -> defaultValue(item.getCorrectCount())).sum();
        return buildItem(name, questionCount, correctCount);
    }

    /**
     * 构建业务处理结果。
     */
    private PracticeStatisticsItemResp buildItem(String name, int questionCount, int correctCount) {
        PracticeStatisticsItemResp item = new PracticeStatisticsItemResp();
        item.setName(name);
        item.setQuestionCount(questionCount);
        item.setCorrectCount(correctCount);
        item.setWrongCount(Math.max(0, questionCount - correctCount));
        item.setAccuracyRate(questionCount == 0 ? 0 : (int) Math.round(correctCount * 100.0 / questionCount));
        return item;
    }

    private PracticeTrendResp buildTrend(List<PracticeSession> sessions,
                                         List<PracticeSessionQuestion> questions,
                                         int periodDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart = now.minusDays(periodDays);
        LocalDateTime previousStart = now.minusDays(periodDays * 2L);
        List<PracticeSessionQuestion> currentQuestions = questions.stream()
                .filter(item -> item.getAnswerTime() != null && !item.getAnswerTime().isBefore(currentStart))
                .collect(Collectors.toList());
        List<PracticeSessionQuestion> previousQuestions = questions.stream()
                .filter(item -> item.getAnswerTime() != null
                        && !item.getAnswerTime().isBefore(previousStart)
                        && item.getAnswerTime().isBefore(currentStart))
                .collect(Collectors.toList());
        int currentAccuracy = accuracyRate(currentQuestions);
        int previousAccuracy = accuracyRate(previousQuestions);
        PracticeTrendResp trend = new PracticeTrendResp();
        trend.setPeriodDays(periodDays);
        trend.setSessionCount((int) sessions.stream()
                .filter(item -> item.getCreateTime() != null && !item.getCreateTime().isBefore(currentStart))
                .count());
        trend.setAnsweredCount(currentQuestions.size());
        int correctCount = (int) currentQuestions.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsCorrect()))
                .count();
        trend.setCorrectCount(correctCount);
        trend.setWrongCount(Math.max(0, currentQuestions.size() - correctCount));
        trend.setAccuracyRate(currentAccuracy);
        trend.setAccuracyRateDelta(currentAccuracy - previousAccuracy);
        return trend;
    }

    /**
     * 标准化并计算业务数据。
     */
    private int accuracyRate(List<PracticeSessionQuestion> questions) {
        if (questions.isEmpty()) {
            return 0;
        }
        long correctCount = questions.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsCorrect()))
                .count();
        return (int) Math.round(correctCount * 100.0 / questions.size());
    }

    /**
     * 标准化并计算业务数据。
     */
    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}
