package com.study.module.system.review.service.impl;

import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.review.dto.response.PracticeQuestionResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.constants.PracticeQuestionSource;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专项练习响应组装工具
 */
final class PracticeSessionSupport {

    private static final Set<String> AUTO_JUDGE_QUESTION_TYPES = new HashSet<>(Arrays.asList(
            "选择题", "判断题", "填空题", "单选题", "多选题"));

    private PracticeSessionSupport() {
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions) {
        return buildDetail(session, sessionQuestions, wrongQuestions, java.util.Collections.emptyList());
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions,
                                                 List<QuestionBank> bankQuestions) {
        return buildDetail(session, sessionQuestions, wrongQuestions, bankQuestions, false);
    }

    static PracticeSessionDetailResp buildDetail(PracticeSession session,
                                                 List<PracticeSessionQuestion> sessionQuestions,
                                                 List<WrongQuestion> wrongQuestions,
                                                 List<QuestionBank> bankQuestions,
                                                 boolean includeAnswers) {
        Map<Long, WrongQuestion> questionMap = wrongQuestions.stream()
                .collect(Collectors.toMap(WrongQuestion::getId, question -> question, (left, right) -> left));
        Map<Long, QuestionBank> bankQuestionMap = bankQuestions.stream()
                .collect(Collectors.toMap(QuestionBank::getId, question -> question, (left, right) -> left));
        PracticeSessionDetailResp response = new PracticeSessionDetailResp();
        response.setSessionId(session.getId());
        response.setTitle(session.getTitle());
        response.setGenerationReason(session.getGenerationReason());
        response.setPracticeType(session.getPracticeType());
        response.setQuestionCount(session.getQuestionCount());
        response.setAnsweredCount(session.getAnsweredCount());
        response.setCorrectCount(session.getCorrectCount());
        response.setWrongCount(session.getWrongCount());
        response.setAccuracyRate(session.getAccuracyRate());
        response.setStatus(session.getStatus());
        response.setQuestionList(sessionQuestions.stream()
                .map(item -> buildQuestion(item, questionMap.get(item.getWrongQuestionId()),
                        bankQuestionMap.get(item.getBankQuestionId()), includeAnswers))
                .collect(Collectors.toList()));
        return response;
    }

    private static PracticeQuestionResp buildQuestion(PracticeSessionQuestion sessionQuestion,
                                                      WrongQuestion question,
                                                      QuestionBank bankQuestion,
                                                      boolean includeAnswers) {
        PracticeQuestionResp response = new PracticeQuestionResp();
        response.setSessionQuestionId(sessionQuestion.getId());
        response.setWrongQuestionId(sessionQuestion.getWrongQuestionId());
        response.setQuestionSource(sessionQuestion.getQuestionSource());
        response.setSourceReason(sessionQuestion.getSourceReason());
        response.setBankQuestionId(sessionQuestion.getBankQuestionId());
        response.setQuestionTitle(sessionQuestion.getQuestionTitleSnapshot());
        response.setSubjectName(sessionQuestion.getSubjectName());
        response.setLearningPoint(sessionQuestion.getLearningPoint());
        response.setErrorLabelSnapshot(sessionQuestion.getErrorLabelSnapshot());
        response.setDifficulty(sessionQuestion.getDifficulty());
        response.setAnswered(sessionQuestion.getAnswerTime() != null);
        response.setStudentAnswer(sessionQuestion.getStudentAnswer());
        response.setIsCorrect(sessionQuestion.getIsCorrect());
        response.setDurationSeconds(sessionQuestion.getDurationSeconds());
        response.setAnswerTime(sessionQuestion.getAnswerTime());
        if (question != null) {
            response.setQuestionContent(question.getQuestionContent());
            response.setQuestionTypeName(question.getQuestionTypeName());
            response.setImageUrl(question.getImageUrl());
            response.setImageUrl2(question.getImageUrl2());
            response.setImageUrl3(question.getImageUrl3());
            response.setImageUrl4(question.getImageUrl4());
            if (includeAnswers || sessionQuestion.getAnswerTime() != null) {
                response.setCorrectAnswer(question.getCorrectAnswer());
                response.setAnalysis(question.getAnalysis());
            }
            if (!StringUtils.hasText(response.getQuestionTitle())) {
                response.setQuestionTitle(question.getQuestionTitle());
            }
            response.setJudgeMode(isAutoJudge(question.getQuestionTypeName(), null) ? "AUTO" : "SELF");
            response.setAutoJudge(isAutoJudge(question.getQuestionTypeName(), null));
        }
        if (bankQuestion != null) {
            response.setQuestionContent(bankQuestion.getQuestionContent());
            response.setQuestionTypeName(bankQuestion.getQuestionTypeName());
            response.setImageUrls(bankQuestion.getImageUrls());
            response.setContentFormat(bankQuestion.getContentFormat());
            response.setOptionsJson(bankQuestion.getOptionsJson());
            response.setJudgeMode(bankQuestion.getJudgeMode());
            fillBankImages(response, bankQuestion.getImageUrls());
            if (includeAnswers || sessionQuestion.getAnswerTime() != null) {
                response.setCorrectAnswer(bankQuestion.getCorrectAnswer());
                response.setAnalysis(bankQuestion.getAnalysis());
            }
            if (!StringUtils.hasText(response.getQuestionTitle())) {
                response.setQuestionTitle(bankQuestion.getQuestionTitle());
            }
            response.setAutoJudge(isAutoJudge(bankQuestion.getQuestionTypeName(), bankQuestion.getJudgeMode()));
        }
        if (!StringUtils.hasText(response.getQuestionSource())) {
            response.setQuestionSource(response.getBankQuestionId() == null
                    ? PracticeQuestionSource.WRONG_QUESTION : PracticeQuestionSource.QUESTION_BANK);
        }
        return response;
    }

    private static boolean isAutoJudge(String questionTypeName, String judgeMode) {
        return "AUTO".equals(judgeMode) || AUTO_JUDGE_QUESTION_TYPES.contains(questionTypeName);
    }

    /**
     * 更新业务数据。
     */
    private static void fillBankImages(PracticeQuestionResp response, String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return;
        }
        String[] images = imageUrls.split(",");
        if (images.length > 0) {
            response.setImageUrl(images[0].trim());
        }
        if (images.length > 1) {
            response.setImageUrl2(images[1].trim());
        }
        if (images.length > 2) {
            response.setImageUrl3(images[2].trim());
        }
        if (images.length > 3) {
            response.setImageUrl4(images[3].trim());
        }
    }
}
