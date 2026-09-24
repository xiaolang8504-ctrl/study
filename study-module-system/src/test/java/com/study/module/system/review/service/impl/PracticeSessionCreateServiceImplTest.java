package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.review.dto.request.PracticeQuestionSelectReq;
import com.study.module.system.review.dto.request.PracticeSessionCreateReq;
import com.study.module.system.review.dto.response.PracticeSessionPreviewResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 专项练习组卷规则测试。
 */
@ExtendWith(MockitoExtension.class)
class PracticeSessionCreateServiceImplTest {

    @Mock
    private PracticeSessionService practiceSessionService;
    @Mock
    private PracticeSessionQuestionService practiceSessionQuestionService;
    @Mock
    private ReviewItemService reviewItemService;
    @Mock
    private WrongQuestionService wrongQuestionService;
    @Mock
    private QuestionBankService questionBankService;
    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<WrongQuestion> wrongQuestionQuery;
    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<ReviewItem> reviewItemQuery;
    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<PracticeSessionQuestion> practiceQuestionQuery;
    @InjectMocks
    private PracticeSessionCreateServiceImpl practiceSessionCreateService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 预览会去除同题候选并如实返回题量缺口，不会伪造题目填满请求题数。
     */
    @Test
    void shouldReturnQuestionShortageInPreview() {
        login();
        prepareDuplicateWrongQuestions();

        PracticeSessionPreviewResp response = practiceSessionCreateService.previewPracticeSession(request(2));

        assertEquals(2, response.getRequestedQuestionCount());
        assertEquals(1, response.getAvailableQuestionCount());
        assertEquals(1, response.getShortageQuestionCount());
        assertEquals(1, response.getWrongQuestionCount());
        assertEquals(0, response.getBankQuestionCount());
        assertEquals("一次函数", response.getLearningPointList().get(0));
    }

    /**
     * 题量不足时创建接口必须失败，不能留下不完整练习会话。
     */
    @Test
    void shouldRejectCreatingIncompletePracticeSession() {
        login();
        prepareDuplicateWrongQuestions();

        assertThrows(LogicException.class,
                () -> practiceSessionCreateService.createPracticeSession(request(2)));

        verify(practiceSessionService, never()).save(org.mockito.ArgumentMatchers.any(PracticeSession.class));
    }

    /**
     * 手工组卷篮必须按学生调整后的顺序返回，不能再次被智能排序覆盖。
     */
    @Test
    void shouldPreserveQuestionBasketOrder() {
        login();
        WrongQuestion first = question(1L, "第一题");
        WrongQuestion second = question(2L, "第二题");
        when(wrongQuestionService.lambdaQuery()).thenReturn(wrongQuestionQuery);
        when(wrongQuestionQuery.list()).thenReturn(Arrays.asList(first, second));
        PracticeSessionCreateReq request = request(2);
        request.setSelectedQuestionList(Arrays.asList(selected(2L), selected(1L)));

        PracticeSessionPreviewResp response = practiceSessionCreateService.previewPracticeSession(request);

        assertEquals(2L, response.getQuestionList().get(0).getQuestionId());
        assertEquals(1L, response.getQuestionList().get(1).getQuestionId());
        assertEquals("第二题", response.getQuestionList().get(0).getQuestionTitle());
    }

    private void login() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(8L, null, Collections.emptyList()));
    }

    private void prepareDuplicateWrongQuestions() {
        WrongQuestion question = new WrongQuestion();
        question.setId(1L);
        question.setQuestionTitle("一次函数图像");
        question.setQuestionContent("求一次函数解析式");
        question.setLearningPoint("一次函数");
        question.setStatus(1);
        question.setLevel(3);
        WrongQuestion duplicate = new WrongQuestion();
        duplicate.setId(2L);
        duplicate.setQuestionTitle(question.getQuestionTitle());
        duplicate.setQuestionContent(question.getQuestionContent());
        duplicate.setLearningPoint(question.getLearningPoint());
        duplicate.setStatus(1);
        duplicate.setLevel(3);
        when(wrongQuestionService.lambdaQuery()).thenReturn(wrongQuestionQuery);
        when(wrongQuestionQuery.list()).thenReturn(java.util.Arrays.asList(question, duplicate));
        when(reviewItemService.lambdaQuery()).thenReturn(reviewItemQuery);
        when(reviewItemQuery.list()).thenReturn(Collections.emptyList());
        when(practiceSessionQuestionService.lambdaQuery()).thenReturn(practiceQuestionQuery);
        when(practiceQuestionQuery.list()).thenReturn(Collections.emptyList());
    }

    private PracticeSessionCreateReq request(int questionCount) {
        PracticeSessionCreateReq request = new PracticeSessionCreateReq();
        request.setPracticeType("KNOWLEDGE");
        request.setQuestionSource("WRONG_QUESTION");
        request.setLearningPoint("一次函数");
        request.setQuestionCount(questionCount);
        return request;
    }

    private PracticeQuestionSelectReq selected(Long questionId) {
        PracticeQuestionSelectReq selected = new PracticeQuestionSelectReq();
        selected.setQuestionSource("WRONG_QUESTION");
        selected.setQuestionId(questionId);
        return selected;
    }

    private WrongQuestion question(Long id, String title) {
        WrongQuestion question = new WrongQuestion();
        question.setId(id);
        question.setQuestionTitle(title);
        question.setQuestionContent(title + "内容");
        question.setStatus(1);
        question.setLevel(3);
        return question;
    }
}
