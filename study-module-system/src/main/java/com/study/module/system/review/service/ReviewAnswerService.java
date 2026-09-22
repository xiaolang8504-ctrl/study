package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.ReviewAnswerResp;

/**
 * 查看复习答案服务
 */
public interface ReviewAnswerService {

    /**
     * 保存答案曝光前的作答凭证。
     */
    void saveReviewAnswerDraft(Long reviewItemId, String studentAnswer);

    /**
     * 查看复习答案
     */
    ReviewAnswerResp reviewAnswer(Long reviewItemId);
}
