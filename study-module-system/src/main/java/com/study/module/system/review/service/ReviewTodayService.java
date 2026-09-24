package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.ReviewTodayHomeResp;

/**
 * 今日复习服务
 */
public interface ReviewTodayService {

    /**
     * 今日复习首页
     */
    ReviewTodayHomeResp todayReviewHome(String subject);

    /**
     * 今日复习首页；任务包场景可传入题量上限，只取当前优先级最高的若干题。
     */
    ReviewTodayHomeResp todayReviewHome(String subject, Integer taskLimit);
}
