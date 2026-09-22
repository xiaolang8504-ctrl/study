package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.constants.PracticeSessionStatus;
import com.study.module.system.review.constants.PracticeQuestionSource;
import com.study.module.system.review.constants.ReviewAnswerJudgeType;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.review.dto.request.PracticeAnswerBatchSubmitReq;
import com.study.module.system.review.dto.request.PracticeAnswerDraftItemReq;
import com.study.module.system.review.dto.request.PracticeAnswerDraftSaveReq;
import com.study.module.system.review.dto.request.PracticeAnswerSubmitReq;
import com.study.module.system.review.dto.response.PracticeAnswerSubmitResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.entity.PracticeAnswerRecord;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticeAnswerRecordService;
import com.study.module.system.review.service.PracticeAnswerSubmitService;
import com.study.module.system.review.service.PracticeProgressSyncService;
import com.study.module.system.review.service.PracticeSessionDetailService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.PracticeWrongQuestionCollectService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专项练习作答服务实现
 */
@Service
public class PracticeAnswerSubmitServiceImpl implements PracticeAnswerSubmitService {

    private static final Set<String> AUTO_JUDGE_QUESTION_TYPES = new HashSet<>(Arrays.asList(
            "选择题", "判断题", "填空题", "单选题", "多选题"));

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    PracticeSessionQuestionService practiceSessionQuestionService;

    @Autowired
    PracticeAnswerRecordService practiceAnswerRecordService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    PracticeProgressSyncService practiceProgressSyncService;

    @Autowired
    PracticeSessionDetailService practiceSessionDetailService;

    @Autowired
    PracticeWrongQuestionCollectService practiceWrongQuestionCollectService;

    /**
     * 执行 submitPracticeAnswer 业务处理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeAnswerSubmitResp submitPracticeAnswer(PracticeAnswerSubmitReq request) {
        Long userId = AccountUtils.getUserId();
        SubmitResult submitResult = submitOne(userId, null, request, LocalDateTime.now());
        refreshSessionStatistics(submitResult.sessionId, userId);
        return submitResult.response;
    }

    /**
     * 执行 batchSubmitPracticeAnswer 业务处理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSessionDetailResp batchSubmitPracticeAnswer(PracticeAnswerBatchSubmitReq request) {
        Long userId = AccountUtils.getUserId();
        checkSession(request.getSessionId(), userId);
        LocalDateTime now = LocalDateTime.now();
        for (PracticeAnswerSubmitReq answer : request.getAnswerList()) {
            submitOne(userId, request.getSessionId(), answer, now);
        }
        refreshSessionStatistics(request.getSessionId(), userId);
        return practiceSessionDetailService.practiceSessionDetail(request.getSessionId());
    }

    /**
     * 创建或保存练习。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSessionDetailResp savePracticeAnswerDraft(PracticeAnswerDraftSaveReq request) {
        Long userId = AccountUtils.getUserId();
        checkSession(request.getSessionId(), userId);
        Map<Long, PracticeAnswerDraftItemReq> draftMap = request.getDraftList().stream()
                .collect(Collectors.toMap(PracticeAnswerDraftItemReq::getSessionQuestionId,
                        item -> item, (left, right) -> right));
        if (draftMap.isEmpty()) {
            return practiceSessionDetailService.practiceSessionDetail(request.getSessionId());
        }
        List<PracticeSessionQuestion> sessionQuestions = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getSessionId, request.getSessionId())
                .eq(PracticeSessionQuestion::getUserId, userId)
                .in(PracticeSessionQuestion::getId, draftMap.keySet())
                .list();
        for (PracticeSessionQuestion sessionQuestion : sessionQuestions) {
            if (sessionQuestion.getAnswerTime() != null) {
                continue;
            }
            PracticeAnswerDraftItemReq draft = draftMap.get(sessionQuestion.getId());
            sessionQuestion.setStudentAnswer(draft.getStudentAnswer());
            sessionQuestion.setDurationSeconds(defaultValue(draft.getDurationSeconds()));
            practiceSessionQuestionService.updateById(sessionQuestion);
        }
        return practiceSessionDetailService.practiceSessionDetail(request.getSessionId());
    }

    private SubmitResult submitOne(Long userId, Long expectedSessionId, PracticeAnswerSubmitReq request,
                                   LocalDateTime now) {
        PracticeSessionQuestion sessionQuestion = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getId, request.getSessionQuestionId())
                .eq(PracticeSessionQuestion::getUserId, userId)
                .one();
        if (sessionQuestion == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        if (sessionQuestion.getAnswerTime() != null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_SUBMITTED);
        }
        PracticeSession session = practiceSessionService.lambdaQuery()
                .eq(PracticeSession::getId, sessionQuestion.getSessionId())
                .eq(PracticeSession::getUserId, userId)
                .one();
        if (session == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_NOT_EXIST);
        }
        if (expectedSessionId != null && !expectedSessionId.equals(session.getId())) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        if (Integer.valueOf(PracticeSessionStatus.FINISHED).equals(session.getStatus())) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_FINISHED);
        }
        PracticeQuestionContext question = loadQuestionContext(sessionQuestion, userId);
        AnswerResult answerResult = judgeAnswer(request, question);
        Long collectedWrongQuestionId = collectWrongQuestionIfNecessary(question, userId,
                answerResult.correct, request.getStudentAnswer());
        sessionQuestion.setStudentAnswer(request.getStudentAnswer());
        sessionQuestion.setIsCorrect(answerResult.correct);
        if (collectedWrongQuestionId != null) {
            sessionQuestion.setWrongQuestionId(collectedWrongQuestionId);
        }
        sessionQuestion.setDurationSeconds(defaultValue(request.getDurationSeconds()));
        sessionQuestion.setAnswerTime(now);
        practiceSessionQuestionService.updateById(sessionQuestion);

        PracticeAnswerRecord record = new PracticeAnswerRecord();
        record.setSessionId(session.getId());
        record.setSessionQuestionId(sessionQuestion.getId());
        record.setUserId(userId);
        record.setWrongQuestionId(collectedWrongQuestionId == null
                ? sessionQuestion.getWrongQuestionId() : collectedWrongQuestionId);
        record.setQuestionSource(question.questionSource);
        record.setBankQuestionId(sessionQuestion.getBankQuestionId());
        record.setStudentAnswer(request.getStudentAnswer());
        record.setIsCorrect(answerResult.correct);
        record.setJudgeType(answerResult.judgeType);
        record.setDurationSeconds(defaultValue(request.getDurationSeconds()));
        record.setAnswerTime(now);
        record.setCreateTime(now);
        practiceAnswerRecordService.save(record);
        if (question.wrongQuestion != null) {
            practiceProgressSyncService.syncPracticeAnswer(question.wrongQuestion.getId(), userId,
                    answerResult.correct == 1);
        }

        PracticeAnswerSubmitResp response = new PracticeAnswerSubmitResp();
        response.setCorrect(answerResult.correct == 1);
        response.setCorrectAnswer(question.correctAnswer());
        response.setAnalysis(question.analysis());
        response.setJudgeType(answerResult.judgeType == ReviewAnswerJudgeType.AUTO ? "AUTO" : "SELF");
        response.setAutoCollectedWrongQuestion(collectedWrongQuestionId != null);
        response.setWrongQuestionId(collectedWrongQuestionId);
        return new SubmitResult(session.getId(), response);
    }

    /**
     * 处理业务数据。
     */
    private Long collectWrongQuestionIfNecessary(PracticeQuestionContext question, Long userId, int correct,
                                                 String studentAnswer) {
        if (correct == 1 || question.bankQuestion == null) {
            return null;
        }
        return practiceWrongQuestionCollectService.collectBankQuestionWrong(question.bankQuestion, userId,
                studentAnswer);
    }

    /**
     * 校验业务数据。
     */
    private PracticeSession checkSession(Long sessionId, Long userId) {
        PracticeSession session = practiceSessionService.lambdaQuery()
                .eq(PracticeSession::getId, sessionId)
                .eq(PracticeSession::getUserId, userId)
                .one();
        if (session == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_NOT_EXIST);
        }
        if (Integer.valueOf(PracticeSessionStatus.FINISHED).equals(session.getStatus())) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_FINISHED);
        }
        return session;
    }

    /**
     * 查询业务数据。
     */
    private PracticeQuestionContext loadQuestionContext(PracticeSessionQuestion sessionQuestion, Long userId) {
        if (sessionQuestion.getBankQuestionId() != null) {
            QuestionBank bankQuestion = questionBankService.lambdaQuery()
                    .eq(QuestionBank::getId, sessionQuestion.getBankQuestionId())
                    .eq(QuestionBank::getReviewStatus, 1)
                    .eq(QuestionBank::getEnable, 1)
                    .and(query -> query.isNull(QuestionBank::getExpireAt)
                            .or().ge(QuestionBank::getExpireAt, java.time.LocalDate.now()))
                    .one();
            if (bankQuestion == null) {
                throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
            }
            return PracticeQuestionContext.of(bankQuestion);
        }
        WrongQuestion wrongQuestion = wrongQuestionService.getById(sessionQuestion.getWrongQuestionId());
        if (wrongQuestion == null || !userId.equals(wrongQuestion.getCreateId())) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        return PracticeQuestionContext.of(wrongQuestion);
    }

    /**
     * 处理业务数据。
     */
    private AnswerResult judgeAnswer(PracticeAnswerSubmitReq request, PracticeQuestionContext question) {
        if (question.autoJudge()
                && question.correctAnswer() != null && !question.correctAnswer().trim().isEmpty()) {
            boolean correct = normalize(request.getStudentAnswer()).equals(normalize(question.correctAnswer()));
            return new AnswerResult(correct ? 1 : 0, ReviewAnswerJudgeType.AUTO);
        }
        if (request.getSelfCorrect() == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_RESULT_REQUIRED);
        }
        return new AnswerResult(request.getSelfCorrect(), ReviewAnswerJudgeType.SELF);
    }

    /**
     * 更新业务数据。
     */
    private void refreshSessionStatistics(Long sessionId, Long userId) {
        int answeredCount = Math.toIntExact(practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getSessionId, sessionId)
                .eq(PracticeSessionQuestion::getUserId, userId)
                .isNotNull(PracticeSessionQuestion::getAnswerTime)
                .count());
        int correctCount = Math.toIntExact(practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getSessionId, sessionId)
                .eq(PracticeSessionQuestion::getUserId, userId)
                .eq(PracticeSessionQuestion::getIsCorrect, 1)
                .count());
        int totalDuration = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getSessionId, sessionId)
                .eq(PracticeSessionQuestion::getUserId, userId)
                .list().stream().mapToInt(item -> defaultValue(item.getDurationSeconds())).sum();
        PracticeSession session = practiceSessionService.getById(sessionId);
        session.setAnsweredCount(answeredCount);
        session.setCorrectCount(correctCount);
        session.setWrongCount(Math.max(0, answeredCount - correctCount));
        session.setTotalDurationSeconds(totalDuration);
        session.setAccuracyRate(answeredCount == 0 ? 0 : (int) Math.round(correctCount * 100.0 / answeredCount));
        session.setUpdateTime(LocalDateTime.now());
        practiceSessionService.updateById(session);
    }

    /**
     * 标准化并计算业务数据。
     */
    private String normalize(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.replaceAll("<[^>]+>", "")
                .replaceAll("[\\s，,。；;：:、]", "")
                .toUpperCase(Locale.ROOT)
                .trim();
    }

    /**
     * 标准化并计算业务数据。
     */
    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private static class AnswerResult {
        private final int correct;
        private final int judgeType;

        /**
         * 执行 AnswerResult 辅助处理。
         */
        private AnswerResult(int correct, int judgeType) {
            this.correct = correct;
            this.judgeType = judgeType;
        }
    }

    private static class SubmitResult {
        private final Long sessionId;
        private final PracticeAnswerSubmitResp response;

        /**
         * 执行 SubmitResult 辅助处理。
         */
        private SubmitResult(Long sessionId, PracticeAnswerSubmitResp response) {
            this.sessionId = sessionId;
            this.response = response;
        }
    }

    private static class PracticeQuestionContext {
        private final String questionSource;
        private final WrongQuestion wrongQuestion;
        private final QuestionBank bankQuestion;

        private PracticeQuestionContext(String questionSource, WrongQuestion wrongQuestion,
                                        QuestionBank bankQuestion) {
            this.questionSource = questionSource;
            this.wrongQuestion = wrongQuestion;
            this.bankQuestion = bankQuestion;
        }

        /**
         * 执行 of 辅助处理。
         */
        private static PracticeQuestionContext of(WrongQuestion question) {
            return new PracticeQuestionContext(PracticeQuestionSource.WRONG_QUESTION, question, null);
        }

        /**
         * 执行 of 辅助处理。
         */
        private static PracticeQuestionContext of(QuestionBank question) {
            return new PracticeQuestionContext(PracticeQuestionSource.QUESTION_BANK, null, question);
        }

        /**
         * 执行 autoJudge 辅助处理。
         */
        private boolean autoJudge() {
            if (bankQuestion != null) {
                return "AUTO".equals(bankQuestion.getJudgeMode())
                        || AUTO_JUDGE_QUESTION_TYPES.contains(bankQuestion.getQuestionTypeName());
            }
            return AUTO_JUDGE_QUESTION_TYPES.contains(wrongQuestion.getQuestionTypeName());
        }

        /**
         * 执行 correctAnswer 辅助处理。
         */
        private String correctAnswer() {
            return bankQuestion == null ? wrongQuestion.getCorrectAnswer() : bankQuestion.getCorrectAnswer();
        }

        /**
         * 执行 analysis 辅助处理。
         */
        private String analysis() {
            return bankQuestion == null ? wrongQuestion.getAnalysis() : bankQuestion.getAnalysis();
        }
    }
}
