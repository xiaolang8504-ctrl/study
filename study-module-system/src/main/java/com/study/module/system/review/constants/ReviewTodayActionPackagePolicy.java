package com.study.module.system.review.constants;

import com.study.module.system.review.dto.response.ReviewLearningPointResp;
import com.study.module.system.review.dto.response.ReviewTodayActionPackageResp;
import com.study.module.system.review.dto.response.ReviewTodayActionResp;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 将今日已有的到期复习、待订正和薄弱知识点组织为不同时间预算的任务包。
 */
public final class ReviewTodayActionPackagePolicy {

    public static final String REVIEW = "REVIEW";
    public static final String CORRECTION = "CORRECTION";
    public static final String WEAK_POINT_PRACTICE = "WEAK_POINT_PRACTICE";

    private static final List<Integer> BUDGET_MINUTES = Arrays.asList(10, 20, 30);
    private static final int CORRECTION_MINUTES_PER_QUESTION = 4;
    private static final int MAX_WEAK_POINT_QUESTION_COUNT = 5;

    private ReviewTodayActionPackagePolicy() {
    }

    /**
     * 每个包优先安排到期复习，同时在预算允许时至少放入一题订正和一题薄弱点练习，
     * 使短时间学习不只停留在单一环节。
     */
    public static List<ReviewTodayActionPackageResp> build(int reviewMinutesPerQuestion,
                                                            long dueReviewCount,
                                                            long overdueReviewCount,
                                                            long pendingCorrectionCount,
                                                            ReviewLearningPointResp weakestPoint,
                                                            String subject) {
        int reviewMinutes = Math.max(1, reviewMinutesPerQuestion);
        List<ReviewTodayActionPackageResp> packages = new ArrayList<>();
        for (Integer budget : BUDGET_MINUTES) {
            packages.add(buildPackage(budget, reviewMinutes, dueReviewCount, overdueReviewCount,
                    pendingCorrectionCount, weakestPoint, subject));
        }
        return packages;
    }

    private static ReviewTodayActionPackageResp buildPackage(int budgetMinutes,
                                                              int reviewMinutesPerQuestion,
                                                              long dueReviewCount,
                                                              long overdueReviewCount,
                                                              long pendingCorrectionCount,
                                                              ReviewLearningPointResp weakestPoint,
                                                              String subject) {
        int remainingMinutes = budgetMinutes;
        List<ReviewTodayActionResp> actions = new ArrayList<>();

        // 先各安排一小步，保证任务包同时覆盖复习、订正和薄弱点行动。
        remainingMinutes -= appendAction(actions, REVIEW, 1, dueReviewCount,
                reviewMinutesPerQuestion, overdueReviewCount, subject, null, remainingMinutes);
        remainingMinutes -= appendAction(actions, CORRECTION, 1, pendingCorrectionCount,
                CORRECTION_MINUTES_PER_QUESTION, 0, subject, null, remainingMinutes);
        remainingMinutes -= appendAction(actions, WEAK_POINT_PRACTICE, 1,
                weakPointQuestionLimit(weakestPoint), reviewMinutesPerQuestion, 0, subject,
                weakestPoint == null ? null : weakestPoint.getLearningPoint(), remainingMinutes);

        // 剩余时间仍按到期复习、订正、薄弱点的顺序补齐，保持遗忘风险优先。
        remainingMinutes -= appendAction(actions, REVIEW, Integer.MAX_VALUE, dueReviewCount,
                reviewMinutesPerQuestion, overdueReviewCount, subject, null, remainingMinutes);
        remainingMinutes -= appendAction(actions, CORRECTION, Integer.MAX_VALUE, pendingCorrectionCount,
                CORRECTION_MINUTES_PER_QUESTION, 0, subject, null, remainingMinutes);
        appendAction(actions, WEAK_POINT_PRACTICE, Integer.MAX_VALUE,
                weakPointQuestionLimit(weakestPoint), reviewMinutesPerQuestion, 0, subject,
                weakestPoint == null ? null : weakestPoint.getLearningPoint(), remainingMinutes);

        ReviewTodayActionPackageResp response = new ReviewTodayActionPackageResp();
        response.setBudgetMinutes(budgetMinutes);
        response.setEstimatedMinutes(actions.stream().mapToInt(ReviewTodayActionResp::getEstimatedMinutes).sum());
        response.setTitle(budgetMinutes + " 分钟任务包");
        response.setActionList(actions);
        response.setRecommendation(buildRecommendation(actions, budgetMinutes));
        fillNextActionText(actions);
        return response;
    }

    private static int appendAction(List<ReviewTodayActionResp> actions, String actionType,
                                    int requestedCount, long availableCount, int minutesPerQuestion,
                                    long overdueReviewCount, String subject, String learningPoint,
                                    int remainingMinutes) {
        if (availableCount <= 0 || remainingMinutes < minutesPerQuestion) {
            return 0;
        }
        ReviewTodayActionResp action = findAction(actions, actionType);
        int alreadySelected = action == null ? 0 : action.getQuestionCount();
        long selectableCount = Math.max(0L, availableCount - alreadySelected);
        int selectedCount = (int) Math.min(Math.min((long) requestedCount, selectableCount),
                remainingMinutes / minutesPerQuestion);
        if (selectedCount <= 0) {
            return 0;
        }
        if (action == null) {
            action = new ReviewTodayActionResp();
            action.setActionType(actionType);
            action.setSubject(subject);
            action.setLearningPoint(learningPoint);
            actions.add(action);
        }
        action.setQuestionCount(alreadySelected + selectedCount);
        action.setEstimatedMinutes(action.getQuestionCount() * minutesPerQuestion);
        action.setTitle(actionTitle(actionType, action.getQuestionCount(), learningPoint));
        action.setReason(actionReason(actionType, overdueReviewCount, learningPoint));
        return selectedCount * minutesPerQuestion;
    }

    private static ReviewTodayActionResp findAction(List<ReviewTodayActionResp> actions, String actionType) {
        return actions.stream().filter(item -> actionType.equals(item.getActionType())).findFirst().orElse(null);
    }

    private static long weakPointQuestionLimit(ReviewLearningPointResp weakestPoint) {
        if (weakestPoint == null || !StringUtils.hasText(weakestPoint.getLearningPoint())
                || "未分类".equals(weakestPoint.getLearningPoint())) {
            return 0;
        }
        long questionCount = weakestPoint.getQuestionCount() == null ? 0 : weakestPoint.getQuestionCount();
        return Math.min(MAX_WEAK_POINT_QUESTION_COUNT, questionCount);
    }

    private static String actionTitle(String actionType, int questionCount, String learningPoint) {
        if (CORRECTION.equals(actionType)) {
            return "订正 " + questionCount + " 道新错题";
        }
        if (WEAK_POINT_PRACTICE.equals(actionType)) {
            return "练习「" + learningPoint + "」" + questionCount + " 题";
        }
        return "完成 " + questionCount + " 道到期复习";
    }

    private static String actionReason(String actionType, long overdueReviewCount, String learningPoint) {
        if (CORRECTION.equals(actionType)) {
            return "订正完成后会自动进入复习计划，避免新错题长期搁置。";
        }
        if (WEAK_POINT_PRACTICE.equals(actionType)) {
            return "「" + learningPoint + "」是当前已订正错题中掌握度较低的知识点。";
        }
        return overdueReviewCount > 0 ? "其中包含逾期题，先处理可降低继续遗忘的风险。"
                : "按当前到期顺序安排，完成后会依据反馈重新排期。";
    }

    private static String buildRecommendation(List<ReviewTodayActionResp> actions, int budgetMinutes) {
        if (actions.isEmpty()) {
            return "今天暂无到期复习、待订正或可练习的薄弱知识点；可先录入新的错题。";
        }
        int estimatedMinutes = actions.stream().mapToInt(ReviewTodayActionResp::getEstimatedMinutes).sum();
        return "预计约 " + estimatedMinutes + " 分钟（预算 " + budgetMinutes
                + " 分钟），请从第一项开始；每完成一步都会给出下一项建议。";
    }

    private static void fillNextActionText(List<ReviewTodayActionResp> actions) {
        for (int index = 0; index < actions.size(); index++) {
            ReviewTodayActionResp action = actions.get(index);
            action.setNextActionText(index + 1 < actions.size()
                    ? "完成后建议继续：" + actions.get(index + 1).getTitle()
                    : "完成后刷新今日首页，系统会根据最新作答重新推荐下一步。");
        }
    }
}
