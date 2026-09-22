package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionReportHandleReq;
import com.study.module.system.questionbank.entity.QuestionReport;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionReportMapper;
import com.study.module.system.questionbank.service.QuestionReportHandleService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 题目举报处理服务实现
 */
@Service
public class QuestionReportHandleServiceImpl extends ServiceImpl<QuestionReportMapper, QuestionReport>
        implements QuestionReportHandleService {

    @Autowired
    QuestionBankService questionBankService;

    /**
     * 处理题目举报
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleQuestionReport(QuestionReportHandleReq request) {
        QuestionReport report = getById(request.getId());
        if (report == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        report.setStatus(request.getStatus());
        report.setHandlerId(AccountUtils.getUserId());
        report.setHandleRemark(request.getHandleRemark());
        report.setHandleTime(LocalDateTime.now());
        updateById(report);
        if (Boolean.TRUE.equals(request.getDisableQuestion()) && request.getStatus() == 1) {
            QuestionBank question = questionBankService.getById(report.getBankQuestionId());
            if (question == null) {
                throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
            }
            question.setEnable(0);
            question.setUpdateTime(LocalDateTime.now());
            questionBankService.updateById(question);
        }
    }
}
