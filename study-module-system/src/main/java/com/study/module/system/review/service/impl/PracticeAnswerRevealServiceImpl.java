package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.review.dto.response.PracticeQuestionAnswerResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticeAnswerRevealService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 查看专项练习参考答案服务实现。
 */
@Service
public class PracticeAnswerRevealServiceImpl implements PracticeAnswerRevealService {

    private static final Set<String> AUTO_JUDGE_QUESTION_TYPES = new HashSet<>(Arrays.asList(
            "选择题", "判断题", "填空题", "单选题", "多选题"));

    @Autowired
    PracticeSessionQuestionService practiceSessionQuestionService;

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    QuestionBankService questionBankService;

    /**
     * 查看本人练习题目的标准答案，供主观题完成自评。
     */
    @Override
    public PracticeQuestionAnswerResp practiceQuestionAnswer(Long sessionQuestionId) {
        Long userId = AccountUtils.getUserId();
        PracticeSessionQuestion sessionQuestion = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getId, sessionQuestionId)
                .eq(PracticeSessionQuestion::getUserId, userId)
                .one();
        if (sessionQuestion == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        PracticeSession session = practiceSessionService.lambdaQuery()
                .eq(PracticeSession::getId, sessionQuestion.getSessionId())
                .eq(PracticeSession::getUserId, userId)
                .one();
        if (session == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_NOT_EXIST);
        }
        PracticeQuestionAnswerResp response = new PracticeQuestionAnswerResp();
        if (sessionQuestion.getBankQuestionId() != null) {
            QuestionBank question = questionBankService.lambdaQuery()
                    .eq(QuestionBank::getId, sessionQuestion.getBankQuestionId())
                    .eq(QuestionBank::getReviewStatus, 1)
                    .eq(QuestionBank::getEnable, 1)
                    .and(query -> query.isNull(QuestionBank::getExpireAt)
                            .or().ge(QuestionBank::getExpireAt, java.time.LocalDate.now()))
                    .one();
            if (question == null) {
                throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
            }
            response.setCorrectAnswer(question.getCorrectAnswer());
            response.setAnalysis(question.getAnalysis());
            response.setAutoJudge(isAutoJudge(question.getQuestionTypeName(), question.getJudgeMode()));
            return response;
        }
        WrongQuestion question = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getId, sessionQuestion.getWrongQuestionId())
                .eq(WrongQuestion::getCreateId, userId)
                .one();
        if (question == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setAnalysis(question.getAnalysis());
        response.setAutoJudge(isAutoJudge(question.getQuestionTypeName(), null));
        return response;
    }

    private boolean isAutoJudge(String questionTypeName, String judgeMode) {
        return "AUTO".equals(judgeMode) || AUTO_JUDGE_QUESTION_TYPES.contains(questionTypeName);
    }
}
