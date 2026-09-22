package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.ReviewLearningReportReq;
import com.study.module.system.review.dto.response.ReviewLearningReportResp;

/**
 * 学情报告服务。
 */
public interface ReviewLearningReportService {

    /**
     * 查询当前学生的学情报告。
     */
    ReviewLearningReportResp reviewLearningReport(ReviewLearningReportReq request);
}
