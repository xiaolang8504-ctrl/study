package com.study.module.system.review.service;

import com.study.module.system.questionbank.entity.QuestionBank;

/**
 * 专项练习错题沉淀服务
 */
public interface PracticeWrongQuestionCollectService {

    /**
     * 将题库练习错题沉淀到个人错题本
     */
    Long collectBankQuestionWrong(QuestionBank questionBank, Long userId, String studentAnswer);
}
