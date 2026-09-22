package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionBankReviewReq;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankReviewService;
import com.study.module.system.questionbank.service.QuestionBankHistoryService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 题库题目审核服务实现
 */
@Service
public class QuestionBankReviewServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankReviewService {

    @Autowired
    QuestionBankHistoryService questionBankHistoryService;

    /**
     * 审核题库题目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewQuestionBank(QuestionBankReviewReq request) {
        QuestionBank entity = getById(request.getId());
        if (entity == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        entity.setReviewStatus(request.getReviewStatus());
        entity.setReviewRemark(request.getReviewRemark());
        Long reviewerId = AccountUtils.getUserId();
        entity.setReviewerId(reviewerId);
        entity.setReviewTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        if (!updateById(entity)) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_REVIEW_FAIL);
        }
        questionBankHistoryService.createReviewLog(request, reviewerId);
    }
}
