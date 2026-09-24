package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 即使练习会话因历史数据错误引用了另一学生的错题，也不暴露其内容或答案。 */
@ExtendWith(MockitoExtension.class)
class PracticeSessionDetailServiceImplTest {
    @Mock private PracticeSessionService practiceSessionService;
    @Mock private PracticeSessionQuestionService practiceSessionQuestionService;
    @Mock private WrongQuestionService wrongQuestionService;
    @Mock private QuestionBankService questionBankService;
    @InjectMocks private PracticeSessionDetailServiceImpl service;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crossOwnerQuestionFailsForDetailAndExport() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(100L, null));
        PracticeSession session = new PracticeSession();
        session.setId(1L);
        session.setUserId(100L);
        PracticeSessionQuestion sessionQuestion = new PracticeSessionQuestion();
        sessionQuestion.setWrongQuestionId(3L);
        WrongQuestion otherStudentsQuestion = new WrongQuestion();
        otherStudentsQuestion.setId(3L);
        otherStudentsQuestion.setCreateId(200L);
        otherStudentsQuestion.setCorrectAnswer("private answer");
        when(practiceSessionService.getOne(any(Wrapper.class))).thenReturn(session);
        when(practiceSessionQuestionService.list(any(Wrapper.class)))
                .thenReturn(Collections.singletonList(sessionQuestion));
        when(wrongQuestionService.listByIds(Collections.singletonList(3L)))
                .thenReturn(Collections.singletonList(otherStudentsQuestion));

        assertThrows(LogicException.class, () -> service.practiceSessionDetail(1L));
        assertThrows(LogicException.class, () -> service.practicePaperDetail(1L));
    }
}
