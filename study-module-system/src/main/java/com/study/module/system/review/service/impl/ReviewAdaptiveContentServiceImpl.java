package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewFeedback;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.dto.response.ReviewAdaptiveContentItemResp;
import com.study.module.system.review.dto.response.ReviewAdaptiveContentResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.service.ReviewAdaptiveContentService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自适应学习内容服务实现
 */
@Service
public class ReviewAdaptiveContentServiceImpl implements ReviewAdaptiveContentService {

    private static final int RECOMMEND_LIMIT = 6;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    ReviewRecordService reviewRecordService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    /**
     * 生成自适应学习内容
     */
    @Override
    public ReviewAdaptiveContentResp adaptiveLearningContent(String subject) {
        Long userId = AccountUtils.getUserId();
        String selectedSubject = StringUtils.hasText(subject) ? subject.trim() : null;
        List<ReviewItem> items = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId)
                .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                .list();
        if (items.isEmpty()) {
            return emptyResponse();
        }

        Map<Long, ReviewItem> itemMap = items.stream().collect(Collectors.toMap(
                ReviewItem::getWrongQuestionId, item -> item, (left, right) -> left));
        List<WrongQuestion> questions = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .in(WrongQuestion::getId, itemMap.keySet())
                .in(WrongQuestion::getStatus, 1, 2)
                .eq(StringUtils.hasText(selectedSubject), WrongQuestion::getSubject, selectedSubject)
                .list();
        if (questions.isEmpty()) {
            return emptyResponse();
        }

        // 每道错题只取最近一次作答，用真实正确性和反馈决定下一步内容方向。
        Map<Long, ReviewRecord> latestRecordMap = new HashMap<>();
        reviewRecordService.lambdaQuery()
                .eq(ReviewRecord::getUserId, userId)
                .in(ReviewRecord::getWrongQuestionId,
                        questions.stream().map(WrongQuestion::getId).collect(Collectors.toSet()))
                .orderByDesc(ReviewRecord::getReviewTime)
                .list().forEach(record -> latestRecordMap.putIfAbsent(
                        record.getWrongQuestionId(), record));

        LocalDateTime now = LocalDateTime.now();
        List<Candidate> candidates = questions.stream()
                .map(question -> buildCandidate(question, itemMap.get(question.getId()),
                        latestRecordMap.get(question.getId()), now))
                .sorted(Comparator.comparingInt(Candidate::getScore).reversed())
                .collect(Collectors.toList());
        List<Candidate> selected = selectWithRotation(candidates);

        ReviewAdaptiveContentResp response = new ReviewAdaptiveContentResp();
        response.setStrategyDescription("系统根据最近真实作答动态切换基础巩固、到期复习和难度提升，并轮换不同知识点。");
        response.setContentList(selected.stream().map(this::buildResponse)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * 构建自适应学习候选内容
     */
    private Candidate buildCandidate(WrongQuestion question, ReviewItem item,
                                     ReviewRecord latestRecord, LocalDateTime now) {
        int score = 0;
        String actionType = "REVIEW";
        List<String> reasons = new ArrayList<>();
        if (latestRecord != null && Integer.valueOf(0).equals(latestRecord.getIsCorrect())) {
            score += 45;
            actionType = "CONSOLIDATE";
            reasons.add("最近一次作答错误");
        } else if (latestRecord != null && Integer.valueOf(1).equals(latestRecord.getIsCorrect())
                && Integer.valueOf(ReviewFeedback.EASY).equals(latestRecord.getFeedback())) {
            score += 18;
            actionType = "CHALLENGE";
            reasons.add("最近答对且反馈很简单，可提高难度");
        } else if (latestRecord != null
                && Integer.valueOf(ReviewFeedback.DIFFICULT).equals(latestRecord.getFeedback())) {
            score += 35;
            actionType = "CONSOLIDATE";
            reasons.add("最近反馈困难");
        }
        if (item.getNextReviewTime() != null && item.getNextReviewTime().isBefore(now)) {
            score += 30;
            reasons.add("已经到期");
        }
        if (defaultValue(item.getLapseCount()) > 0) {
            score += Math.min(defaultValue(item.getLapseCount()), 3) * 10;
            reasons.add("曾遗忘" + item.getLapseCount() + "次");
        }
        if (!Integer.valueOf(2).equals(question.getStatus())) {
            score += 20;
            reasons.add("尚未掌握");
        }
        // 一天内刚复习过的题适当降权，让内容在多个错题间轮换。
        if (item.getLastReviewTime() != null
                && item.getLastReviewTime().isAfter(now.minusDays(1))) {
            score -= 20;
        }
        if (reasons.isEmpty()) {
            reasons.add("按长期记忆计划巩固");
        }
        return new Candidate(question, score, actionType, String.join("、", reasons));
    }

    /**
     * 先按知识点各取一道，再按得分补足，避免推荐流被单一知识点占满。
     */
    private List<Candidate> selectWithRotation(List<Candidate> candidates) {
        List<Candidate> selected = new ArrayList<>();
        Set<String> points = new HashSet<>();
        for (Candidate candidate : candidates) {
            if (selected.size() >= RECOMMEND_LIMIT) break;
            if (points.add(pointKey(candidate.question))) selected.add(candidate);
        }
        Set<Long> ids = selected.stream().map(candidate -> candidate.question.getId())
                .collect(Collectors.toSet());
        for (Candidate candidate : candidates) {
            if (selected.size() >= RECOMMEND_LIMIT) break;
            if (ids.add(candidate.question.getId())) selected.add(candidate);
        }
        return selected;
    }

    /**
     * 构建响应数据
     */
    private ReviewAdaptiveContentItemResp buildResponse(Candidate candidate) {
        ReviewAdaptiveContentItemResp response = new ReviewAdaptiveContentItemResp();
        response.setWrongQuestionId(candidate.question.getId());
        response.setQuestionTitle(candidate.question.getQuestionTitle());
        response.setSubject(candidate.question.getSubject());
        response.setSubjectName(candidate.question.getSubjectName());
        response.setLearningPoint(candidate.question.getLearningPoint());
        response.setLevel(candidate.question.getLevel());
        response.setActionType(candidate.actionType);
        response.setActionText(actionText(candidate.actionType));
        response.setRecommendationReason(candidate.reason);
        return response;
    }

    /**
     * 获取操作文案
     */
    private String actionText(String actionType) {
        if ("CONSOLIDATE".equals(actionType)) return "基础巩固";
        if ("CHALLENGE".equals(actionType)) return "难度提升";
        return "到期复习";
    }

    /**
     * 生成知识点分组键
     */
    private String pointKey(WrongQuestion question) {
        return StringUtils.hasText(question.getLearningPoint())
                ? question.getLearningPoint().trim() : "未分类";
    }

    /**
     * 获取默认值
     */
    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 构建空响应
     */
    private ReviewAdaptiveContentResp emptyResponse() {
        ReviewAdaptiveContentResp response = new ReviewAdaptiveContentResp();
        response.setStrategyDescription("暂无可推荐内容，请先完成错题订正。 ");
        response.setContentList(new ArrayList<>());
        return response;
    }

    private static class Candidate {
        private final WrongQuestion question;
        private final int score;
        private final String actionType;
        private final String reason;

        /**
         * 执行 Candidate 辅助处理。
         */
        private Candidate(WrongQuestion question, int score, String actionType, String reason) {
            this.question = question;
            this.score = score;
            this.actionType = actionType;
            this.reason = reason;
        }

        /**
         * 获取题目得分
         */
        private int getScore() {
            return score;
        }
    }
}
