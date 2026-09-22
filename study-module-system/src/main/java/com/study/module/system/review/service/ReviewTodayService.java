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
}
