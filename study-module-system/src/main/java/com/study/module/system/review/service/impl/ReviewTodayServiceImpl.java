package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.response.ReviewHomeResp;
import com.study.module.system.review.dto.response.ReviewTodayHomeResp;
import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.constants.ReviewDefault;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.service.ReviewHomeService;
import com.study.module.system.review.service.ReviewPlanService;
import com.study.module.system.review.service.ReviewTodayTaskListService;
import com.study.module.system.review.service.ReviewTodayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 今日复习服务实现
 */
@Service
public class ReviewTodayServiceImpl implements ReviewTodayService {

    @Autowired
    ReviewHomeService reviewHomeService;

    @Autowired
    ReviewPlanService reviewPlanService;

    @Autowired
    ReviewTodayTaskListService reviewTodayTaskListService;

    /**
     * 查询今日复习首页
     */
    @Override
    public ReviewTodayHomeResp todayReviewHome(String subject) {
        // 初始化计划并汇总今日统计，确保新订正的错题能够及时进入复习队列。
        ReviewHomeResp reviewHome = reviewHomeService.initializeReviewHome(subject);
        LocalDate today = LocalDate.now();
        LocalDateTime currentTime = LocalDateTime.now();
        ReviewPlan reviewPlan = reviewPlanService.getById(reviewHome.getPlanId());
        List<ReviewTodayTaskResp> taskList = reviewTodayTaskListService.todayReviewTaskList(
                reviewPlan, subject, today, currentTime);
        long remainingCount = taskList.size();
        long todayTaskCount = reviewHome.getCompletedCount() + remainingCount;

        ReviewTodayHomeResp response = new ReviewTodayHomeResp();
        response.setReviewDate(today);
        response.setTotalCount(todayTaskCount);
        response.setCompletedCount(reviewHome.getCompletedCount());
        response.setRemainingCount(remainingCount);
        response.setOverdueCount(reviewHome.getOverdueCount());
        response.setEstimatedMinutes(Math.toIntExact(
                remainingCount * ReviewDefault.ESTIMATED_MINUTES_PER_QUESTION));
        response.setProgressRate(calculateProgressRate(reviewHome.getCompletedCount(),
                todayTaskCount));
        response.setContinuousReviewDays(reviewHome.getContinuousReviewDays());
        response.setDailyLimit(reviewHome.getDailyLimit());
        response.setTaskList(taskList);
        response.setSelectedSubject(reviewHome.getSelectedSubject());
        response.setSubjectSettings(reviewHome.getSubjectSettings());
        return response;
    }

    /**
     * 计算今日完成百分比，保留一位小数并处理无任务场景。
     */
    private BigDecimal calculateProgressRate(long completedCount, long totalCount) {
        if (totalCount == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(completedCount * 100)
                .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
    }
}
