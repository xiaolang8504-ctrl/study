package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.request.ReviewLearningReportReq;
import com.study.module.system.review.dto.response.ReviewLearningReportResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.service.LearningMetricDailyService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionErrorAnalysisService;
import com.study.module.system.wrongquestion.service.WrongQuestionKnowledgePointBindService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 学情报告聚合测试。
 */
@ExtendWith(MockitoExtension.class)
class ReviewLearningReportServiceImplTest {

    @Mock
    private WrongQuestionService wrongQuestionService;
    @Mock
    private ReviewRecordService reviewRecordService;
    @Mock
    private WrongQuestionKnowledgePointBindService wrongQuestionKnowledgePointBindService;
    @Mock
    private WrongQuestionErrorAnalysisService wrongQuestionErrorAnalysisService;
    @Mock
    private LearningMetricDailyService learningMetricDailyService;
    @Mock
    private ReviewItemService reviewItemService;
    @Mock
    private WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;
    @InjectMocks
    private ReviewLearningReportServiceImpl reviewLearningReportService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 报告只在当前登录用户的数据基础上汇总，并将掌握率最低的知识点置顶。
     */
    @Test
    void shouldBuildLearningReportAndSortWeakPoints() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(8L, null, Collections.emptyList()));
        when(wrongQuestionService.list(org.mockito.ArgumentMatchers.<Wrapper<WrongQuestion>>any())).thenReturn(Arrays.asList(
                wrongQuestion(WrongQuestionStatus.PENDING_CORRECTION),
                wrongQuestion(WrongQuestionStatus.CORRECTED),
                wrongQuestion(WrongQuestionStatus.MASTERED),
                wrongQuestion(WrongQuestionStatus.MASTERED)));
        when(reviewRecordService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewRecord>>any())).thenReturn(Arrays.asList(
                reviewRecord(1), reviewRecord(0), reviewRecord(null)));
        when(wrongQuestionKnowledgePointBindService.wrongQuestionKnowledgePointStatistics(any()))
                .thenReturn(Arrays.asList(point("已掌握", 4, 4), point("薄弱点", 5, 1)));
        when(wrongQuestionErrorAnalysisService.wrongQuestionErrorAnalysisStatistics(any()))
                .thenReturn(Collections.emptyList());
        when(learningMetricDailyService.learningMetricTrend(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ReviewLearningReportResp response = reviewLearningReportService.reviewLearningReport(
                new ReviewLearningReportReq());

        assertEquals(4, response.getWrongQuestionCount());
        assertEquals(1, response.getPendingCorrectionCount());
        assertEquals(50, response.getMasteryRate());
        assertEquals(2, response.getReviewCount30Days());
        assertEquals(50, response.getRetentionRate30Days());
        assertEquals("薄弱点", response.getWeakLearningPointList().get(0).getKnowledgePointName());
    }

    /**
     * 保持率下钻只使用当前报告时间窗内已有独立作答凭证的题目，并优先列出正确率低的题目。
     */
    @Test
    void shouldBuildLowRetentionActionQuestionsInWeakestFirstOrder() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(8L, null, Collections.emptyList()));
        WrongQuestion wrongFirst = wrongQuestion(WrongQuestionStatus.CORRECTED);
        wrongFirst.setId(101L);
        wrongFirst.setQuestionTitle("易忘题");
        WrongQuestion wrongSecond = wrongQuestion(WrongQuestionStatus.CORRECTED);
        wrongSecond.setId(102L);
        wrongSecond.setQuestionTitle("保持较好题");
        when(wrongQuestionService.list(org.mockito.ArgumentMatchers.<Wrapper<WrongQuestion>>any()))
                .thenReturn(Arrays.asList(wrongFirst, wrongSecond));
        when(reviewRecordService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewRecord>>any())).thenReturn(Arrays.asList(
                reviewRecord(101L, 0), reviewRecord(102L, 1), reviewRecord(102L, 1)));
        when(reviewItemService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewItem>>any()))
                .thenReturn(Collections.emptyList());

        ReviewLearningReportReq request = new ReviewLearningReportReq();
        request.setActionType("LOW_RETENTION");
        ReviewLearningReportResp response = reviewLearningReportService.reviewLearningReport(request);

        assertEquals("LOW_RETENTION", response.getActionDetail().getActionType());
        assertEquals(2, response.getActionDetail().getQuestionCount());
        assertEquals(101L, response.getActionDetail().getQuestionList().get(0).getWrongQuestionId());
        assertEquals(0, response.getActionDetail().getQuestionList().get(0).getIndependentCorrectCount30Days());
        assertEquals(1, response.getActionDetail().getQuestionList().get(0).getIndependentReviewCount30Days());
    }

    /**
     * 知识点和错因下钻都只能从当前报告已经筛出的个人错题中取题。
     */
    @Test
    void shouldBuildKnowledgePointAndErrorLabelActionQuestions() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(8L, null, Collections.emptyList()));
        WrongQuestion matchedQuestion = wrongQuestion(WrongQuestionStatus.CORRECTED);
        matchedQuestion.setId(101L);
        matchedQuestion.setQuestionTitle("函数计算题");
        matchedQuestion.setErrorLabels("计算,审题");
        WrongQuestion otherQuestion = wrongQuestion(WrongQuestionStatus.CORRECTED);
        otherQuestion.setId(102L);
        otherQuestion.setErrorLabels("概念");
        when(wrongQuestionService.list(org.mockito.ArgumentMatchers.<Wrapper<WrongQuestion>>any()))
                .thenReturn(Arrays.asList(matchedQuestion, otherQuestion));
        when(reviewRecordService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewRecord>>any()))
                .thenReturn(Collections.emptyList());
        when(reviewItemService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewItem>>any()))
                .thenReturn(Collections.emptyList());
        WrongQuestionKnowledgePoint relation = new WrongQuestionKnowledgePoint();
        relation.setWrongQuestionId(101L);
        relation.setKnowledgePointId(12L);
        when(wrongQuestionKnowledgePointService.list(
                org.mockito.ArgumentMatchers.<Wrapper<WrongQuestionKnowledgePoint>>any()))
                .thenReturn(Collections.singletonList(relation));

        ReviewLearningReportReq knowledgePointRequest = new ReviewLearningReportReq();
        knowledgePointRequest.setActionType("KNOWLEDGE_POINT");
        knowledgePointRequest.setKnowledgePointId(12L);
        ReviewLearningReportResp knowledgePointResponse = reviewLearningReportService.reviewLearningReport(
                knowledgePointRequest);
        assertEquals(1, knowledgePointResponse.getActionDetail().getQuestionCount());
        assertEquals(101L, knowledgePointResponse.getActionDetail().getQuestionList().get(0).getWrongQuestionId());

        ReviewLearningReportReq errorLabelRequest = new ReviewLearningReportReq();
        errorLabelRequest.setActionType("ERROR_LABEL");
        errorLabelRequest.setErrorLabel("审题");
        ReviewLearningReportResp errorLabelResponse = reviewLearningReportService.reviewLearningReport(errorLabelRequest);
        assertEquals(1, errorLabelResponse.getActionDetail().getQuestionCount());
        assertEquals(101L, errorLabelResponse.getActionDetail().getQuestionList().get(0).getWrongQuestionId());
    }

    private WrongQuestion wrongQuestion(int status) {
        WrongQuestion question = new WrongQuestion();
        question.setStatus(status);
        return question;
    }

    private ReviewRecord reviewRecord(Integer isCorrect) {
        return reviewRecord(null, isCorrect);
    }

    private ReviewRecord reviewRecord(Long wrongQuestionId, Integer isCorrect) {
        ReviewRecord record = new ReviewRecord();
        record.setWrongQuestionId(wrongQuestionId);
        record.setIsCorrect(isCorrect);
        record.setIsIndependent(1);
        return record;
    }

    private WrongQuestionKnowledgePointStatisticsResp point(String name, int total, int mastered) {
        WrongQuestionKnowledgePointStatisticsResp response = new WrongQuestionKnowledgePointStatisticsResp();
        response.setKnowledgePointName(name);
        response.setWrongQuestionCount(total);
        response.setMasteredCount(mastered);
        return response;
    }
}
