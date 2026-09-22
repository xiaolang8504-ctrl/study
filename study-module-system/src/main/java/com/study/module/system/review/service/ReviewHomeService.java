package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.ReviewHomeResp;

/**
 * 智能复习首页服务
 */
public interface ReviewHomeService {

    /**
     * 初始化并查询当前用户智能复习首页
     */
    ReviewHomeResp initializeReviewHome();

    /**
     * 初始化并按科目查询当前用户智能复习首页。
     */
    ReviewHomeResp initializeReviewHome(String subject);
}
