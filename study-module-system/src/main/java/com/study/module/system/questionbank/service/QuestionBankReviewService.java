package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionBankReviewReq;
import com.study.module.system.questionbank.entity.QuestionBank;

/**
 * 题库题目审核服务
 */
public interface QuestionBankReviewService extends IService<QuestionBank> {

    /**
     * 审核题库题目
     */
    void reviewQuestionBank(QuestionBankReviewReq request);
}
