package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.ReviewWeakPointPracticeReq;
import com.study.module.system.review.dto.response.ReviewWeakPointPracticeResp;

/**
 * 薄弱知识点专项练习服务
 */
public interface ReviewWeakPointPracticeService {

    /**
     * 生成薄弱点练习
     */
    ReviewWeakPointPracticeResp generateWeakPointPractice(ReviewWeakPointPracticeReq request);
}
