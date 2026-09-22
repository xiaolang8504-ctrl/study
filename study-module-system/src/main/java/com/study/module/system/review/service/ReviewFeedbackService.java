package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.SubmitReviewFeedbackReq;
import com.study.module.system.review.dto.response.SubmitReviewFeedbackResp;

/**
 * 复习反馈服务
 */
public interface ReviewFeedbackService {

    /**
     * 提交四级反馈
     */
    SubmitReviewFeedbackResp submitReviewFeedback(SubmitReviewFeedbackReq request);
}
