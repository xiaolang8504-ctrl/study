package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionPracticeSubmitReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeSubmitResp;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAnswerViewReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAnswerViewResp;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;

/**
 * 相似题作答提交服务
 */
public interface QuestionPracticeSubmitService extends IService<QuestionRecommendationLog> {

    /**
     * 提交相似题作答结果
     */
    QuestionPracticeSubmitResp submitQuestionPractice(QuestionPracticeSubmitReq request);

    QuestionPracticeAnswerViewResp viewQuestionPracticeAnswer(QuestionPracticeAnswerViewReq request);
}
