package com.study.module.system.questionbank.service.impl;

import com.study.module.system.questionbank.dto.request.QuestionBankBatchReviewReq;
import com.study.module.system.questionbank.dto.request.QuestionBankReviewReq;
import com.study.module.system.questionbank.service.QuestionBankBatchReviewService;
import com.study.module.system.questionbank.service.QuestionBankReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 题库批量审核服务实现。
 */
@Service
public class QuestionBankBatchReviewServiceImpl implements QuestionBankBatchReviewService {
    @Autowired
    QuestionBankReviewService questionBankReviewService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReviewQuestionBank(QuestionBankBatchReviewReq request) {
        request.getIds().stream().distinct().forEach(id -> {
            QuestionBankReviewReq item = new QuestionBankReviewReq();
            item.setId(id);
            item.setReviewStatus(request.getReviewStatus());
            item.setReviewRemark(request.getReviewRemark());
            questionBankReviewService.reviewQuestionBank(item);
        });
    }
}
