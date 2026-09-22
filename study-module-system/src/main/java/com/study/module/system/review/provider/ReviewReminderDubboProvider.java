package com.study.module.system.review.provider;

import com.study.api.provider.ReviewReminderProvider;
import com.study.module.system.review.service.ReviewReminderDispatchService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 智能复习提醒服务提供者
 */
@Service
public class ReviewReminderDubboProvider implements ReviewReminderProvider {

    @Autowired
    ReviewReminderDispatchService reviewReminderDispatchService;

    @Override
    public void dispatchDueReviewReminder() {
        reviewReminderDispatchService.dispatchDueReviewReminder();
    }
}
