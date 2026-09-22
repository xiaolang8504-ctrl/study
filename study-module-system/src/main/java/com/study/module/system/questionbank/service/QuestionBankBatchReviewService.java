package com.study.module.system.questionbank.service;

import com.study.module.system.questionbank.dto.request.QuestionBankBatchReviewReq;

/**
 * 题库批量审核服务。
 */
public interface QuestionBankBatchReviewService {
    void batchReviewQuestionBank(QuestionBankBatchReviewReq request);
}
