package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.ReviewAdaptiveContentResp;

/**
 * 自适应学习内容服务
 */
public interface ReviewAdaptiveContentService {

    /**
     * 生成自适应学习内容
     */
    ReviewAdaptiveContentResp adaptiveLearningContent(String subject);
}
