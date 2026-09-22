package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.dto.request.ReviewTypicalPracticeReq;
import com.study.module.system.review.dto.response.ReviewPracticeQuestionResp;
import com.study.module.system.review.dto.response.ReviewTypicalPracticeResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewTypicalPracticeService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 典型错题练习服务实现
 */
@Service
public class ReviewTypicalPracticeServiceImpl implements ReviewTypicalPracticeService {

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    /**
     * 生成典型题练习
     */
    @Override
    public ReviewTypicalPracticeResp generateTypicalPractice(ReviewTypicalPracticeReq request) {
        Long userId = AccountUtils.getUserId();
        String subject = StringUtils.hasText(request.getSubject()) ? request.getSubject().trim() : null;

        // 先读取当前用户的有效复习项，排除暂停、结束和其他用户的数据。
        List<ReviewItem> reviewItems = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId)
                .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                .list();
        if (reviewItems.isEmpty()) {
            return emptyResponse();
        }
        Map<Long, ReviewItem> itemMap = reviewItems.stream().collect(Collectors.toMap(
                ReviewItem::getWrongQuestionId, item -> item, (left, right) -> left));

        // 只读取本人的已订正/已掌握题；科目条件同样在数据库层过滤。
        List<WrongQuestion> questions = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .in(WrongQuestion::getId, itemMap.keySet())
                .in(WrongQuestion::getStatus, 1, 2)
                .eq(StringUtils.hasText(subject), WrongQuestion::getSubject, subject)
                .list();

        LocalDateTime now = LocalDateTime.now();
        List<Candidate> candidates = questions.stream()
                .map(question -> buildCandidate(question, itemMap.get(question.getId()), now))
                .sorted(Comparator.comparingInt(Candidate::getScore).reversed()
                        .thenComparing(candidate -> candidate.question.getUpdateTime(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        List<Candidate> selected = selectWithKnowledgePointCoverage(
                candidates, request.getQuestionCount());

        ReviewTypicalPracticeResp response = new ReviewTypicalPracticeResp();
        response.setQuestionCount(selected.size());
        response.setLearningPointCount((int) selected.stream()
                .map(candidate -> learningPointKey(candidate.question))
                .distinct().count());
        response.setRecommendation(selected.isEmpty()
                ? "暂无可用典型错题，请先完成错题订正和至少一次复习。"
                : "已综合未掌握状态、逾期、遗忘次数、难度和复习次数选题，并优先覆盖不同知识点。");
        response.setQuestionList(selected.stream().map(this::buildQuestionResponse)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * 第一轮每个知识点最多取一道，第二轮再按总分补足，兼顾代表性和知识点覆盖。
     */
    private List<Candidate> selectWithKnowledgePointCoverage(List<Candidate> candidates, int limit) {
        List<Candidate> selected = new ArrayList<>();
        Set<String> selectedPoints = new HashSet<>();
        for (Candidate candidate : candidates) {
            if (selected.size() >= limit) {
                break;
            }
            if (selectedPoints.add(learningPointKey(candidate.question))) {
                selected.add(candidate);
            }
        }
        Set<Long> selectedQuestionIds = selected.stream()
                .map(candidate -> candidate.question.getId()).collect(Collectors.toSet());
        for (Candidate candidate : candidates) {
            if (selected.size() >= limit) {
                break;
            }
            if (selectedQuestionIds.add(candidate.question.getId())) {
                selected.add(candidate);
            }
        }
        return selected;
    }

    /**
     * 构建候选练习题
     */
    private Candidate buildCandidate(WrongQuestion question, ReviewItem item, LocalDateTime now) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        if (!Integer.valueOf(2).equals(question.getStatus())) {
            score += 30;
            reasons.add("尚未掌握");
        }
        if (item.getNextReviewTime() != null && item.getNextReviewTime().isBefore(now)) {
            score += 20;
            reasons.add("已经到期");
        }
        int lapseCount = defaultValue(item.getLapseCount());
        if (lapseCount > 0) {
            score += Math.min(lapseCount, 3) * 12;
            reasons.add("遗忘" + lapseCount + "次");
        }
        int level = defaultValue(question.getLevel());
        if (level > 0) {
            score += Math.min(level, 5) * 6;
            reasons.add("难度" + level);
        }
        int reviewCount = defaultValue(item.getReviewCount());
        score += Math.min(reviewCount, 5);
        if (reviewCount > 1) {
            reasons.add("已复习" + reviewCount + "次仍需巩固");
        }
        if (reasons.isEmpty()) {
            reasons.add("用于保持长期记忆");
        }
        return new Candidate(question, score, String.join("、", reasons));
    }

    /**
     * 构建题目响应
     */
    private ReviewPracticeQuestionResp buildQuestionResponse(Candidate candidate) {
        WrongQuestion question = candidate.question;
        ReviewPracticeQuestionResp response = new ReviewPracticeQuestionResp();
        response.setWrongQuestionId(question.getId());
        response.setQuestionTitle(question.getQuestionTitle());
        response.setQuestionContent(question.getQuestionContent());
        response.setSubjectName(question.getSubjectName());
        response.setQuestionTypeName(question.getQuestionTypeName());
        response.setLearningPoint(question.getLearningPoint());
        response.setLevel(question.getLevel());
        response.setStatus(question.getStatus());
        response.setImageUrl(question.getImageUrl());
        response.setImageUrl2(question.getImageUrl2());
        response.setImageUrl3(question.getImageUrl3());
        response.setImageUrl4(question.getImageUrl4());
        response.setRepresentativeReason(candidate.reason);
        return response;
    }

    /**
     * 构建空响应
     */
    private ReviewTypicalPracticeResp emptyResponse() {
        ReviewTypicalPracticeResp response = new ReviewTypicalPracticeResp();
        response.setRecommendation("暂无可用典型错题，请先完成错题订正和至少一次复习。");
        response.setLearningPointCount(0);
        response.setQuestionCount(0);
        response.setQuestionList(new ArrayList<>());
        return response;
    }

    /**
     * 生成知识点键
     */
    private String learningPointKey(WrongQuestion question) {
        return StringUtils.hasText(question.getLearningPoint())
                ? question.getLearningPoint().trim() : "未分类";
    }

    /**
     * 获取默认值
     */
    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private static class Candidate {
        private final WrongQuestion question;
        private final int score;
        private final String reason;

        /**
         * 执行 Candidate 辅助处理。
         */
        private Candidate(WrongQuestion question, int score, String reason) {
            this.question = question;
            this.score = score;
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
