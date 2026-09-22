package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewPlanStatus;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.service.ReviewPlanService;
import com.study.module.system.review.service.ReviewReminderDispatchService;
import com.study.module.system.review.service.ReviewReminderSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 复习提醒派发服务实现
 */
@Service
@Slf4j
public class ReviewReminderDispatchServiceImpl implements ReviewReminderDispatchService {

    @Autowired
    ReviewPlanService reviewPlanService;

    @Autowired
    ReviewReminderSendService reviewReminderSendService;

    /**
     * 派发到期复习提醒
     */
    @Override
    public void dispatchDueReviewReminder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        List<ReviewPlan> plans = reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getStatus, ReviewPlanStatus.ENABLED)
                .eq(ReviewPlan::getReminderEnabled, 1)
                .le(ReviewPlan::getReminderTime, now.toLocalTime())
                .list();
        for (ReviewPlan plan : plans) {
            if (!isReviewDay(plan.getReviewWeekDays(), today.getDayOfWeek().getValue())) {
                continue;
            }
            // 单个用户发送失败不能中断整批任务，并单独落库失败记录供后续重试。
            try {
                reviewReminderSendService.sendReviewReminder(plan, today, now);
            } catch (Exception exception) {
                log.error("【智能复习提醒】用户{}提醒发送失败", plan.getUserId(), exception);
                try {
                    reviewReminderSendService.recordSendFailure(plan, today, now, exception);
                } catch (Exception recordException) {
                    log.error("【智能复习提醒】用户{}失败记录保存失败", plan.getUserId(), recordException);
                }
            }
        }
    }

    /**
     * 判断指定日期是否为复习日
     */
    private boolean isReviewDay(String weekDays, int todayWeekDay) {
        return weekDays != null && Arrays.stream(weekDays.split(","))
                .map(String::trim)
                .anyMatch(day -> String.valueOf(todayWeekDay).equals(day));
    }
}
