package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionBankReviewReq;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionvariant.mapper.QuestionVariantRecordMapper;
import com.study.module.system.questionvariant.entity.QuestionVariantRecord;
import com.study.module.system.questionvariant.entity.QuestionVariantAudit;
import com.study.module.system.questionvariant.mapper.QuestionVariantAuditMapper;
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

    @Autowired
    QuestionVariantRecordMapper questionVariantRecordMapper;

    @Autowired
    QuestionVariantAuditMapper questionVariantAuditMapper;

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
        if (entity.getVariantRecordId() != null) {
            QuestionVariantRecord record = questionVariantRecordMapper.selectById(entity.getVariantRecordId());
            if (record != null) {
                record.setAuditStatus(request.getReviewStatus());
                record.setUpdateTime(LocalDateTime.now());
                questionVariantRecordMapper.updateById(record);
                QuestionVariantAudit audit = new QuestionVariantAudit();
                audit.setVariantRecordId(record.getId());
                audit.setAuditAction(request.getReviewStatus() == 1 ? "PASS" : "REJECT");
                audit.setAuditStatus(request.getReviewStatus());
                audit.setAuditRemark(request.getReviewRemark());
                audit.setAuditorId(reviewerId);
                audit.setAuditTime(LocalDateTime.now());
                questionVariantAuditMapper.insert(audit);
            }
        }
        questionBankHistoryService.createReviewLog(request, reviewerId);
    }
}
