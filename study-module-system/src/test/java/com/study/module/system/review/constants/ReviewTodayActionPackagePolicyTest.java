package com.study.module.system.review.constants;

import com.study.module.system.review.dto.response.ReviewLearningPointResp;
import com.study.module.system.review.dto.response.ReviewTodayActionPackageResp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 今日时间任务包的组合策略测试。 */
class ReviewTodayActionPackagePolicyTest {

    @Test
    void shouldBuildThreeTimeBudgetsWithReviewCorrectionAndWeakPointActions() {
        ReviewLearningPointResp weakPoint = new ReviewLearningPointResp();
        weakPoint.setLearningPoint("一次函数");
        weakPoint.setQuestionCount(5L);
        weakPoint.setAverageMasteryScore(new BigDecimal("35.0"));

        List<ReviewTodayActionPackageResp> packages = ReviewTodayActionPackagePolicy.build(
                2, 6, 1, 4, weakPoint, "math");

        assertEquals(3, packages.size());
        assertEquals(10, packages.get(0).getBudgetMinutes());
        assertTrue(packages.get(0).getEstimatedMinutes() <= packages.get(0).getBudgetMinutes());
        assertTrue(packages.get(0).getActionList().stream()
                .anyMatch(item -> ReviewTodayActionPackagePolicy.REVIEW.equals(item.getActionType())));
        assertTrue(packages.get(0).getActionList().stream()
                .anyMatch(item -> ReviewTodayActionPackagePolicy.CORRECTION.equals(item.getActionType())));
        assertTrue(packages.get(0).getActionList().stream()
                .anyMatch(item -> ReviewTodayActionPackagePolicy.WEAK_POINT_PRACTICE.equals(item.getActionType())));
        assertEquals("math", packages.get(0).getActionList().get(0).getSubject());
        assertFalse(packages.get(0).getActionList().get(0).getNextActionText().isEmpty());
    }

    @Test
    void shouldExplainWhenThereIsNoActionableWork() {
        List<ReviewTodayActionPackageResp> packages = ReviewTodayActionPackagePolicy.build(
                2, 0, 0, 0, null, null);

        assertEquals(3, packages.size());
        assertTrue(packages.stream().allMatch(item -> item.getActionList().isEmpty()));
        assertTrue(packages.get(0).getRecommendation().contains("暂无"));
    }
}
