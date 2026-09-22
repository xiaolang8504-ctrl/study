package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionReportCreateReq;
import com.study.module.system.questionbank.entity.QuestionReport;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionReportMapper;
import com.study.module.system.questionbank.service.QuestionReportCreateService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 题目举报新增服务实现
 */
@Service
public class QuestionReportCreateServiceImpl extends ServiceImpl<QuestionReportMapper, QuestionReport>
        implements QuestionReportCreateService {

    @Autowired
    QuestionBankService questionBankService;

    /**
     * 新增题目举报
     */
    @Override
    public void reportQuestion(QuestionReportCreateReq request) {
        Long userId = AccountUtils.getUserId();
        QuestionBank question = questionBankService.getById(request.getBankQuestionId());
        if (question == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        if (count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuestionReport>()
                .eq(QuestionReport::getUserId, userId)
                .eq(QuestionReport::getBankQuestionId, request.getBankQuestionId())
                .eq(QuestionReport::getReportType, request.getReportType())) > 0) {
            throw new LogicException(ErrorCodeConstants.QUESTION_REPORT_DUPLICATE);
        }
        QuestionReport report = new QuestionReport();
        report.setUserId(userId);
        report.setBankQuestionId(request.getBankQuestionId());
        report.setReportType(request.getReportType());
        report.setReportContent(request.getReportContent());
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        save(report);
    }
}
