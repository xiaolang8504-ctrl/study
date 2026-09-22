package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.ReviewTypicalPracticeReq;
import com.study.module.system.review.dto.response.ReviewTypicalPracticeResp;

/**
 * 典型错题练习服务
 */
public interface ReviewTypicalPracticeService {

    /**
     * 生成典型题练习
     */
    ReviewTypicalPracticeResp generateTypicalPractice(ReviewTypicalPracticeReq request);
}
