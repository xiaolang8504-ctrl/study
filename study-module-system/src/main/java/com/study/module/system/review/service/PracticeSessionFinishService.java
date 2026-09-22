package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.PracticeSessionDetailResp;

/**
 * 完成专项练习服务
 */
public interface PracticeSessionFinishService {

    PracticeSessionDetailResp finishPracticeSession(Long sessionId);
}
