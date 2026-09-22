package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.request.ReviewLearningReportReq;
import com.study.module.system.review.dto.response.ReviewLearningReportResp;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.service.LearningMetricDailyService;
import com.study.module.system.review.service.ReviewItemService;
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

    private WrongQuestion wrongQuestion(int status) {
        WrongQuestion question = new WrongQuestion();
        question.setStatus(status);
        return question;
    }

    private ReviewRecord reviewRecord(Integer isCorrect) {
        ReviewRecord record = new ReviewRecord();
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
