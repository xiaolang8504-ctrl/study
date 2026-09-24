package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.review.constants.PracticePaperAnswerStatus;
import com.study.module.system.review.constants.PracticePaperFillMode;
import com.study.module.system.review.dto.request.PracticePaperAnswerFillItemReq;
import com.study.module.system.review.dto.request.PracticePaperAnswerFillReq;
import com.study.module.system.review.dto.response.PracticePaperAnswerRecordResp;
import com.study.module.system.review.dto.response.PracticePaperFillDetailResp;
import com.study.module.system.review.dto.response.PracticePaperFillQuestionResp;
import com.study.module.system.review.entity.PracticePaperAnswerRecord;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticePaperAnswerRecordService;
import com.study.module.system.review.service.PracticePaperFillService;
import com.study.module.system.review.service.PracticeProgressSyncService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.PracticeWrongQuestionCollectService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 纸面练习卷逐题回填服务实现。
 */
@Service
public class PracticePaperFillServiceImpl implements PracticePaperFillService {

    private static final Pattern PAPER_CODE_PATTERN = Pattern.compile("(?i)^P?(\\d{1,19})$");

    private static final String PAPER_PRACTICE_WRONG_EVENT = "PAPER_PRACTICE_WRONG";

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    PracticeSessionQuestionService practiceSessionQuestionService;

    @Autowired
    PracticePaperAnswerRecordService practicePaperAnswerRecordService;

    @Autowired
    PracticeProgressSyncService practiceProgressSyncService;

    @Autowired
    PracticeWrongQuestionCollectService practiceWrongQuestionCollectService;

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    /**
     * 查询纸面练习卷和已有的逐题回填记录。
     */
    @Override
    public PracticePaperFillDetailResp practicePaperFillDetail(String paperCode) {
        Long userId = AccountUtils.getUserId();
        Long sessionId = parsePaperCode(paperCode);
        PracticeSession session = loadSession(sessionId, userId);
        return buildDetail(session, userId, formatPaperCode(sessionId));
    }

    /**
     * 保存一次纸面练习卷逐题回填。
     *
     * <p>新增回填属于一份新的练习证据；覆盖只修订最近一次记录。覆盖不会再次提高
     * 掌握度，避免同一次纸面练习被重复计分；若把原先正确改为错误，则立即重新激活复习。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticePaperFillDetailResp submitPracticePaperAnswerFill(PracticePaperAnswerFillReq request) {
        Long userId = AccountUtils.getUserId();
        Long sessionId = parsePaperCode(request.getPaperCode());
        PracticeSession session = loadSession(sessionId, userId);
        String fillMode = normalizeFillMode(request.getFillMode());
        List<PracticeSessionQuestion> sessionQuestions = loadSessionQuestions(sessionId, userId);
        Map<Long, PracticePaperAnswerFillItemReq> answerMap = validateAndIndexAnswers(
                request.getAnswerList(), sessionQuestions);
        List<PracticePaperAnswerRecord> existingRecords = loadAnswerRecords(sessionId, userId);
        int latestAttemptNo = existingRecords.stream().map(PracticePaperAnswerRecord::getAttemptNo)
                .filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(0);
        if (PracticePaperFillMode.OVERWRITE.equals(fillMode) && latestAttemptNo == 0) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_FILL_OVERWRITE_NOT_EXIST);
        }
        int targetAttemptNo = PracticePaperFillMode.NEW.equals(fillMode)
                ? latestAttemptNo + 1 : latestAttemptNo;
        Map<Long, PracticePaperAnswerRecord> targetAttemptRecordMap = existingRecords.stream()
                .filter(item -> Integer.valueOf(targetAttemptNo).equals(item.getAttemptNo()))
                .collect(Collectors.toMap(PracticePaperAnswerRecord::getSessionQuestionId, item -> item,
                        (left, right) -> left));
        LocalDateTime answerTime = request.getAnswerTime() == null ? LocalDateTime.now() : request.getAnswerTime();
        LocalDateTime now = LocalDateTime.now();
        for (PracticeSessionQuestion sessionQuestion : sessionQuestions) {
            PracticePaperAnswerFillItemReq answer = answerMap.get(sessionQuestion.getId());
            PracticePaperAnswerRecord previous = targetAttemptRecordMap.get(sessionQuestion.getId());
            String previousAnswerStatus = previous == null ? null : previous.getAnswerStatus();
            String answerStatus = normalizeAnswerStatus(answer.getAnswerStatus());
            Long wrongQuestionId = resolveWrongQuestionId(sessionQuestion, answerStatus, userId);
            PracticePaperAnswerRecord record = previous == null ? new PracticePaperAnswerRecord() : previous;
            fillRecord(record, session, sessionQuestion, userId, targetAttemptNo, wrongQuestionId,
                    answer, answerStatus, request.getAnswerFileId(), answerTime, now, previous != null);
            if (previous == null) {
                practicePaperAnswerRecordService.save(record);
            } else {
                practicePaperAnswerRecordService.updateById(record);
            }
            syncLearningProgressIfNecessary(fillMode, previousAnswerStatus, answerStatus, wrongQuestionId, userId);
            recordWrongQuestionTimelineIfNecessary(answerStatus, wrongQuestionId, session,
                    sessionQuestion, answer.getErrorReason(), userId);
        }
        return buildDetail(session, userId, formatPaperCode(sessionId));
    }

    private PracticeSession loadSession(Long sessionId, Long userId) {
        PracticeSession session = practiceSessionService.getOne(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getId, sessionId)
                .eq(PracticeSession::getUserId, userId));
        if (session == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_NOT_EXIST);
        }
        return session;
    }

    private List<PracticeSessionQuestion> loadSessionQuestions(Long sessionId, Long userId) {
        List<PracticeSessionQuestion> questions = practiceSessionQuestionService.list(
                new LambdaQueryWrapper<PracticeSessionQuestion>()
                        .eq(PracticeSessionQuestion::getSessionId, sessionId)
                        .eq(PracticeSessionQuestion::getUserId, userId)
                        .orderByAsc(PracticeSessionQuestion::getSortNo, PracticeSessionQuestion::getId));
        if (questions.isEmpty()) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        return questions;
    }

    private List<PracticePaperAnswerRecord> loadAnswerRecords(Long sessionId, Long userId) {
        return practicePaperAnswerRecordService.list(new LambdaQueryWrapper<PracticePaperAnswerRecord>()
                .eq(PracticePaperAnswerRecord::getSessionId, sessionId)
                .eq(PracticePaperAnswerRecord::getUserId, userId)
                .orderByDesc(PracticePaperAnswerRecord::getAttemptNo)
                .orderByDesc(PracticePaperAnswerRecord::getAnswerTime)
                .orderByDesc(PracticePaperAnswerRecord::getId));
    }

    private Map<Long, PracticePaperAnswerFillItemReq> validateAndIndexAnswers(
            List<PracticePaperAnswerFillItemReq> answerList, List<PracticeSessionQuestion> sessionQuestions) {
        Map<Long, PracticePaperAnswerFillItemReq> answerMap = new LinkedHashMap<>();
        for (PracticePaperAnswerFillItemReq answer : answerList) {
            if (answerMap.put(answer.getSessionQuestionId(), answer) != null) {
                throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
            }
        }
        List<Long> sessionQuestionIds = sessionQuestions.stream().map(PracticeSessionQuestion::getId)
                .collect(Collectors.toList());
        if (answerMap.size() != sessionQuestionIds.size() || !answerMap.keySet().containsAll(sessionQuestionIds)) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        for (PracticePaperAnswerFillItemReq answer : answerMap.values()) {
            normalizeAnswerStatus(answer.getAnswerStatus());
        }
        return answerMap;
    }

    private Long resolveWrongQuestionId(PracticeSessionQuestion sessionQuestion, String answerStatus, Long userId) {
        Long wrongQuestionId = sessionQuestion.getWrongQuestionId();
        if (!PracticePaperAnswerStatus.WRONG.equals(answerStatus) || wrongQuestionId != null
                || sessionQuestion.getBankQuestionId() == null) {
            return wrongQuestionId;
        }
        QuestionBank bankQuestion = questionBankService.getById(sessionQuestion.getBankQuestionId());
        if (bankQuestion == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_NOT_EXIST);
        }
        wrongQuestionId = practiceWrongQuestionCollectService.collectBankQuestionWrong(bankQuestion, userId, "");
        sessionQuestion.setWrongQuestionId(wrongQuestionId);
        practiceSessionQuestionService.updateById(sessionQuestion);
        return wrongQuestionId;
    }

    private void fillRecord(PracticePaperAnswerRecord record, PracticeSession session,
                            PracticeSessionQuestion sessionQuestion, Long userId, int attemptNo,
                            Long wrongQuestionId, PracticePaperAnswerFillItemReq answer, String answerStatus,
                            Long answerFileId, LocalDateTime answerTime, LocalDateTime now,
                            boolean preserveAttachment) {
        record.setSessionId(session.getId());
        record.setPaperVersion(session.getPaperVersion() == null ? 1 : session.getPaperVersion());
        record.setSessionQuestionId(sessionQuestion.getId());
        record.setUserId(userId);
        record.setAttemptNo(attemptNo);
        record.setWrongQuestionId(wrongQuestionId);
        record.setAnswerStatus(answerStatus);
        record.setStudentAnswer(trimToNull(answer.getStudentAnswer()));
        record.setErrorReason(PracticePaperAnswerStatus.WRONG.equals(answerStatus)
                ? trimToNull(answer.getErrorReason()) : null);
        record.setDurationSeconds(answer.getDurationSeconds() == null ? 0 : answer.getDurationSeconds());
        if (answerFileId != null || !preserveAttachment) {
            record.setAnswerFileId(answerFileId);
        }
        record.setAnswerTime(answerTime);
        if (record.getCreateTime() == null) {
            record.setCreateTime(now);
        }
        record.setUpdateTime(now);
    }

    private void syncLearningProgressIfNecessary(String fillMode, String previousAnswerStatus,
                                                  String answerStatus, Long wrongQuestionId, Long userId) {
        if (wrongQuestionId == null || !PracticePaperAnswerStatus.isAnswered(answerStatus)) {
            return;
        }
        if (PracticePaperFillMode.NEW.equals(fillMode) || previousAnswerStatus == null
                || PracticePaperAnswerStatus.UNANSWERED.equals(previousAnswerStatus)
                || PracticePaperAnswerStatus.CORRECT.equals(previousAnswerStatus)
                && PracticePaperAnswerStatus.WRONG.equals(answerStatus)) {
            practiceProgressSyncService.syncPracticeAnswer(wrongQuestionId, userId,
                    PracticePaperAnswerStatus.isCorrect(answerStatus));
        }
    }

    private void recordWrongQuestionTimelineIfNecessary(String answerStatus, Long wrongQuestionId,
                                                         PracticeSession session,
                                                         PracticeSessionQuestion sessionQuestion,
                                                         String errorReason, Long userId) {
        if (!PracticePaperAnswerStatus.WRONG.equals(answerStatus) || wrongQuestionId == null) {
            return;
        }
        String content = "纸面练习卷 v" + (session.getPaperVersion() == null ? 1 : session.getPaperVersion())
                + " 第" + (sessionQuestion.getSortNo() == null ? "?" : sessionQuestion.getSortNo())
                + "题回填为错误";
        String normalizedErrorReason = trimToNull(errorReason);
        if (normalizedErrorReason != null) {
            content += "；错因：" + normalizedErrorReason;
        }
        wrongQuestionTimelineService.record(wrongQuestionId, PAPER_PRACTICE_WRONG_EVENT,
                "STUDENT", content, userId);
    }

    private PracticePaperFillDetailResp buildDetail(PracticeSession session, Long userId, String paperCode) {
        List<PracticeSessionQuestion> sessionQuestions = loadSessionQuestions(session.getId(), userId);
        List<PracticePaperAnswerRecord> records = loadAnswerRecords(session.getId(), userId);
        Map<Long, List<PracticePaperAnswerRecord>> recordMap = new HashMap<>();
        for (PracticePaperAnswerRecord record : records) {
            recordMap.computeIfAbsent(record.getSessionQuestionId(), ignored -> new ArrayList<>()).add(record);
        }
        PracticePaperFillDetailResp response = new PracticePaperFillDetailResp();
        response.setPaperCode(paperCode);
        response.setSessionId(session.getId());
        response.setTitle(session.getTitle());
        response.setPaperVersion(session.getPaperVersion() == null ? 1 : session.getPaperVersion());
        response.setQuestionCount(sessionQuestions.size());
        response.setLatestAttemptNo(records.stream().map(PracticePaperAnswerRecord::getAttemptNo)
                .filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(0));
        response.setHasPreviousRecord(!records.isEmpty());
        int answeredCount = 0;
        int correctCount = 0;
        int wrongCount = 0;
        List<PracticePaperFillQuestionResp> questionList = new ArrayList<>();
        for (PracticeSessionQuestion question : sessionQuestions) {
            List<PracticePaperAnswerRecord> questionRecords = recordMap.getOrDefault(question.getId(),
                    Collections.emptyList());
            PracticePaperAnswerRecord latestRecord = questionRecords.isEmpty() ? null : questionRecords.get(0);
            if (latestRecord != null && PracticePaperAnswerStatus.isAnswered(latestRecord.getAnswerStatus())) {
                answeredCount++;
                if (PracticePaperAnswerStatus.isCorrect(latestRecord.getAnswerStatus())) {
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }
            questionList.add(toQuestionResp(question, questionRecords));
        }
        response.setAnsweredCount(answeredCount);
        response.setCorrectCount(correctCount);
        response.setWrongCount(wrongCount);
        response.setUnansweredCount(Math.max(0, sessionQuestions.size() - answeredCount));
        response.setQuestionList(questionList);
        return response;
    }

    private PracticePaperFillQuestionResp toQuestionResp(PracticeSessionQuestion question,
                                                         List<PracticePaperAnswerRecord> records) {
        PracticePaperFillQuestionResp response = new PracticePaperFillQuestionResp();
        response.setSessionQuestionId(question.getId());
        response.setSortNo(question.getSortNo());
        response.setWrongQuestionId(question.getWrongQuestionId());
        response.setQuestionSource(question.getQuestionSource());
        response.setQuestionTitle(question.getQuestionTitleSnapshot());
        response.setQuestionContent(question.getQuestionContentSnapshot());
        response.setContentFormat(question.getContentFormatSnapshot());
        response.setOptionsJson(question.getOptionsJsonSnapshot());
        response.setSubjectName(question.getSubjectName());
        response.setLearningPoint(question.getLearningPoint());
        response.setAnswerRecordList(records.stream().map(this::toRecordResp).collect(Collectors.toList()));
        return response;
    }

    private PracticePaperAnswerRecordResp toRecordResp(PracticePaperAnswerRecord record) {
        PracticePaperAnswerRecordResp response = new PracticePaperAnswerRecordResp();
        BeanUtils.copyProperties(record, response);
        return response;
    }

    private Long parsePaperCode(String paperCode) {
        Matcher matcher = PAPER_CODE_PATTERN.matcher(paperCode == null ? "" : paperCode.trim());
        if (!matcher.matches()) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_CODE_INVALID);
        }
        try {
            long sessionId = Long.parseLong(matcher.group(1));
            if (sessionId <= 0) {
                throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_CODE_INVALID);
            }
            return sessionId;
        } catch (NumberFormatException exception) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_CODE_INVALID);
        }
    }

    private String formatPaperCode(Long sessionId) {
        return String.format(Locale.ROOT, "P%010d", sessionId);
    }

    private String normalizeFillMode(String fillMode) {
        String normalized = fillMode == null ? "" : fillMode.trim().toUpperCase(Locale.ROOT);
        if (!PracticePaperFillMode.valid(normalized)) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_FILL_MODE_INVALID);
        }
        return normalized;
    }

    private String normalizeAnswerStatus(String answerStatus) {
        String normalized = answerStatus == null ? "" : answerStatus.trim().toUpperCase(Locale.ROOT);
        if (!PracticePaperAnswerStatus.valid(normalized)) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_ANSWER_STATUS_INVALID);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
