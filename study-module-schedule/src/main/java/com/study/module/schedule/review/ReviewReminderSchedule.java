package com.study.module.schedule.review;

import com.study.api.provider.ReviewReminderProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 智能复习提醒定时任务
 */
@Slf4j
@Component
public class ReviewReminderSchedule {

    @DubboReference
    ReviewReminderProvider reviewReminderProvider;

    /**
     * 每10分钟检查一次到期提醒，数据库唯一索引保证同一用户每天只发送一次。
     */
    @Scheduled(cron = "${cron.reviewReminder:0 */10 * * * ?}")
    public void dispatchDueReviewReminder() {
        log.info("【智能复习提醒】开始派发到期提醒");
        reviewReminderProvider.dispatchDueReviewReminder();
        log.info("【智能复习提醒】到期提醒派发完成");
    }
}
