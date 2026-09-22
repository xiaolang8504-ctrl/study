package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticeSessionDetailService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 专项练习详情服务实现
 */
@Service
public class PracticeSessionDetailServiceImpl implements PracticeSessionDetailService {

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    PracticeSessionQuestionService practiceSessionQuestionService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    QuestionBankService questionBankService;

    /**
     * 查询练习详情。
     */
    @Override
    public PracticeSessionDetailResp practiceSessionDetail(Long sessionId) {
        return loadPracticeSessionDetail(sessionId, false);
    }

    /**
     * 当前学生导出个人练习卷时，可读取自己的答案快照；常规作答接口仍保持答案隐藏。
     */
    @Override
    public PracticeSessionDetailResp practicePaperDetail(Long sessionId) {
        return loadPracticeSessionDetail(sessionId, true);
    }

    private PracticeSessionDetailResp loadPracticeSessionDetail(Long sessionId, boolean includeAnswers) {
        Long userId = AccountUtils.getUserId();
        PracticeSession session = practiceSessionService.lambdaQuery()
                .eq(PracticeSession::getId, sessionId)
                .eq(PracticeSession::getUserId, userId)
                .one();
        if (session == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_NOT_EXIST);
        }
        List<PracticeSessionQuestion> sessionQuestions = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getSessionId, sessionId)
                .eq(PracticeSessionQuestion::getUserId, userId)
                .orderByAsc(PracticeSessionQuestion::getSortNo)
                .list();
        List<Long> questionIds = sessionQuestions.stream()
                .map(PracticeSessionQuestion::getWrongQuestionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        List<WrongQuestion> questions = questionIds.isEmpty()
                ? Collections.emptyList() : wrongQuestionService.listByIds(questionIds);
        List<Long> bankQuestionIds = sessionQuestions.stream()
                .map(PracticeSessionQuestion::getBankQuestionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        List<QuestionBank> bankQuestions = bankQuestionIds.isEmpty()
                ? Collections.emptyList() : questionBankService.listByIds(bankQuestionIds);
        return PracticeSessionSupport.buildDetail(session, sessionQuestions, questions, bankQuestions,
                includeAnswers);
    }
}
