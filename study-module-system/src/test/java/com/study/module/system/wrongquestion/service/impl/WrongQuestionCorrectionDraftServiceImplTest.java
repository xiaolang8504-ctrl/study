package com.study.module.system.wrongquestion.service.impl;

import com.study.common.core.exception.LogicException;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionAnswerLayerRevealReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionAnswerLayerResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionTimelineResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 订正答案分层查看规则测试。 */
@ExtendWith(MockitoExtension.class)
class WrongQuestionCorrectionDraftServiceImplTest {

    @Mock
    private WrongQuestionService wrongQuestionService;
    @Mock
    private WrongQuestionTimelineService timelineService;
    private WrongQuestionCorrectionDraftServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null));
        service = new WrongQuestionCorrectionDraftServiceImpl();
        ReflectionTestUtils.setField(service, "wrongQuestionService", wrongQuestionService);
        ReflectionTestUtils.setField(service, "wrongQuestionTimelineService", timelineService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRequirePreviousLayerBeforeRevealingAnswer() {
        WrongQuestion question = question();
        when(wrongQuestionService.checkWrongQuestion(1L)).thenReturn(question);
        when(timelineService.timelineList(1L)).thenReturn(Collections.emptyList());

        WrongQuestionAnswerLayerRevealReq request = request("ANSWER");

        assertThrows(LogicException.class, () -> service.revealAnswerLayer(request));
    }

    @Test
    void shouldRevealStepsAfterHintAndRecordExposure() {
        WrongQuestion question = question();
        WrongQuestionTimelineResp hint = new WrongQuestionTimelineResp();
        hint.setEventType("HINT_REVEALED");
        when(wrongQuestionService.checkWrongQuestion(1L)).thenReturn(question);
        when(timelineService.timelineList(1L)).thenReturn(Collections.singletonList(hint));

        WrongQuestionAnswerLayerResp response = service.revealAnswerLayer(request("STEPS"));

        assertEquals("解题步骤", response.getTitle());
        assertEquals("先移项，再合并同类项。", response.getContent());
        verify(timelineService).record(1L, "STEPS_REVEALED", "STUDENT", "已查看解题步骤", 7L);
    }

    @Test
    void shouldRevealConfiguredCommonMistakeAfterSteps() {
        WrongQuestion question = question();
        question.setCommonMistake("移项必须变号，最后代回原式验算。");
        WrongQuestionTimelineResp steps = new WrongQuestionTimelineResp();
        steps.setEventType("STEPS_REVEALED");
        when(wrongQuestionService.checkWrongQuestion(1L)).thenReturn(question);
        when(timelineService.timelineList(1L)).thenReturn(Collections.singletonList(steps));

        WrongQuestionAnswerLayerResp response = service.revealAnswerLayer(request("COMMON_MISTAKE"));

        assertEquals("易错点", response.getTitle());
        assertEquals("移项必须变号，最后代回原式验算。", response.getContent());
        verify(timelineService).record(1L, "COMMON_MISTAKE_REVEALED", "STUDENT", "已查看易错点", 7L);
    }

    private WrongQuestion question() {
        WrongQuestion question = new WrongQuestion();
        question.setId(1L);
        question.setAnalysis("先移项，再合并同类项。");
        question.setCorrectAnswer("x=1");
        return question;
    }

    private WrongQuestionAnswerLayerRevealReq request(String layer) {
        WrongQuestionAnswerLayerRevealReq request = new WrongQuestionAnswerLayerRevealReq();
        request.setWrongQuestionId(1L);
        request.setLayer(layer);
        return request;
    }
}
