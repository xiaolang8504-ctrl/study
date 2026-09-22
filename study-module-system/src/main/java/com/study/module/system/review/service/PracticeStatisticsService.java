package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.PracticeSessionPageListReq;
import com.study.module.system.review.dto.response.PracticeStatisticsResp;

/**
 * 专项练习统计服务
 */
public interface PracticeStatisticsService {

    /**
     * 专项练习统计
     */
    PracticeStatisticsResp practiceStatistics(PracticeSessionPageListReq request);
}
