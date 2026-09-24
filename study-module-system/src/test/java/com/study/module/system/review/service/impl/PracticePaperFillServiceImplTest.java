package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.study.module.system.review.dto.request.PracticePaperAnswerFillItemReq;
import com.study.module.system.review.dto.request.PracticePaperAnswerFillReq;
import com.study.module.system.review.entity.PracticePaperAnswerRecord;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticePaperAnswerRecordService;
import com.study.module.system.review.service.PracticeProgressSyncService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.PracticeWrongQuestionCollectService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 纸面回填的重复作答和学习进度同步回归测试。
 */
@ExtendWith(MockitoExtension.class)
class PracticePaperFillServiceImplTest {

    @Mock private PracticeSessionService practiceSessionService;
    @Mock private PracticeSessionQuestionService practiceSessionQuestionService;
    @Mock private PracticePaperAnswerRecordService practicePaperAnswerRecordService;
    @Mock private PracticeProgressSyncService practiceProgressSyncService;
    @Mock private PracticeWrongQuestionCollectService practiceWrongQuestionCollectService;
    @Mock private QuestionBankService questionBankService;
    @Mock private WrongQuestionTimelineService wrongQuestionTimelineService;
    @InjectMocks private PracticePaperFillServiceImpl practicePaperFillService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 覆盖纸面记录时，把原先正确改成错误必须重新安排复习；不能因为实体被原地更新而丢失旧状态。
     */
    @Test
    void overwriteCorrectPaperAnswerAsWrongReactivatesReview() {
        login();
        PracticeSession session = session();
        PracticeSessionQuestion sessionQuestion = sessionQuestion();
        PracticePaperAnswerRecord previous = previousCorrectRecord();
        when(practiceSessionService.getOne(any(Wrapper.class))).thenReturn(session);
        when(practiceSessionQuestionService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(sessionQuestion));
        when(practicePaperAnswerRecordService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(previous));

        PracticePaperAnswerFillReq request = request("OVERWRITE", "WRONG");
        practicePaperFillService.submitPracticePaperAnswerFill(request);

        verify(practicePaperAnswerRecordService).updateById(previous);
        verify(practiceProgressSyncService).syncPracticeAnswer(88L, 8L, false);
        verify(wrongQuestionTimelineService).record(eq(88L), eq("PAPER_PRACTICE_WRONG"),
                eq("STUDENT"), org.mockito.ArgumentMatchers.contains("第1题"), eq(8L));
        assertEquals("WRONG", previous.getAnswerStatus());
    }

    /**
     * 未作答只作为完整卷面状态留存，不能伪造成学习掌握证据。
     */
    @Test
    void newUnansweredPaperAnswerDoesNotAdvanceReview() {
        login();
        when(practiceSessionService.getOne(any(Wrapper.class))).thenReturn(session());
        when(practiceSessionQuestionService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(sessionQuestion()));
        when(practicePaperAnswerRecordService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        practicePaperFillService.submitPracticePaperAnswerFill(request("NEW", "UNANSWERED"));

        ArgumentCaptor<PracticePaperAnswerRecord> captor = ArgumentCaptor.forClass(PracticePaperAnswerRecord.class);
        verify(practicePaperAnswerRecordService).save(captor.capture());
        assertEquals("UNANSWERED", captor.getValue().getAnswerStatus());
        assertFalse(captor.getValue().getDurationSeconds() < 0);
        verify(practiceProgressSyncService, never()).syncPracticeAnswer(any(Long.class), any(Long.class), any(Boolean.class));
        verify(wrongQuestionTimelineService, never()).record(any(Long.class), any(String.class), any(String.class),
                any(String.class), any(Long.class));
    }

    private void login() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(8L, null, Collections.emptyList()));
    }

    private PracticeSession session() {
        PracticeSession session = new PracticeSession();
        session.setId(12L);
        session.setUserId(8L);
        session.setTitle("一次函数纸面重练");
        session.setPaperVersion(3);
        return session;
    }

    private PracticeSessionQuestion sessionQuestion() {
        PracticeSessionQuestion question = new PracticeSessionQuestion();
        question.setId(101L);
        question.setSessionId(12L);
        question.setUserId(8L);
        question.setSortNo(1);
        question.setWrongQuestionId(88L);
        question.setQuestionTitleSnapshot("一次函数图像");
        return question;
    }

    private PracticePaperAnswerRecord previousCorrectRecord() {
        PracticePaperAnswerRecord record = new PracticePaperAnswerRecord();
        record.setId(300L);
        record.setSessionId(12L);
        record.setSessionQuestionId(101L);
        record.setUserId(8L);
        record.setAttemptNo(1);
        record.setWrongQuestionId(88L);
        record.setAnswerStatus("CORRECT");
        return record;
    }

    private PracticePaperAnswerFillReq request(String fillMode, String answerStatus) {
        PracticePaperAnswerFillItemReq item = new PracticePaperAnswerFillItemReq();
        item.setSessionQuestionId(101L);
        item.setAnswerStatus(answerStatus);
        item.setDurationSeconds(35);
        item.setErrorReason("计算过程漏了符号");
        PracticePaperAnswerFillReq request = new PracticePaperAnswerFillReq();
        request.setPaperCode("P0000000012");
        request.setFillMode(fillMode);
        request.setAnswerList(Collections.singletonList(item));
        return request;
    }
}
