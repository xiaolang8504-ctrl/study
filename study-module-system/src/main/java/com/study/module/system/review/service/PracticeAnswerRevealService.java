package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.PracticeQuestionAnswerResp;

/**
 * 查看专项练习参考答案服务。
 */
public interface PracticeAnswerRevealService {

    PracticeQuestionAnswerResp practiceQuestionAnswer(Long sessionQuestionId);
}
