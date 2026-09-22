package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.study.module.system.review.entity.LearningMetricDaily;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.mapper.LearningMetricDailyMapper;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 学习日快照统计测试。
 */
@ExtendWith(MockitoExtension.class)
class LearningMetricDailyServiceImplTest {

    @Mock
    private LearningMetricDailyMapper learningMetricDailyMapper;
    @Mock
    private WrongQuestionService wrongQuestionService;
    @Mock
    private ReviewRecordService reviewRecordService;
    @InjectMocks
    private LearningMetricDailyServiceImpl learningMetricDailyService;

    /**
     * 同一学生应同时生成科目与全科汇总快照，复习正确率按已判定样本计算。
     */
    @Test
    void shouldBuildSubjectAndAllSubjectDailyMetrics() {
        when(wrongQuestionService.list()).thenReturn(Arrays.asList(
                wrongQuestion(8L, "math", WrongQuestionStatus.PENDING_CORRECTION),
                wrongQuestion(8L, "math", WrongQuestionStatus.MASTERED)));
        when(reviewRecordService.list(org.mockito.ArgumentMatchers.<Wrapper<ReviewRecord>>any())).thenReturn(Arrays.asList(
                reviewRecord(8L, "math", 1), reviewRecord(8L, "math", 0), reviewRecord(8L, "math", null)));
        when(learningMetricDailyMapper.selectOne(org.mockito.ArgumentMatchers.<Wrapper<LearningMetricDaily>>any(),
                org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn(null);
        when(learningMetricDailyMapper.insertOrUpdate(any(LearningMetricDaily.class))).thenReturn(true);

        learningMetricDailyService.buildDailySnapshot(LocalDate.of(2026, 9, 8));

        ArgumentCaptor<LearningMetricDaily> captor = ArgumentCaptor.forClass(LearningMetricDaily.class);
        verify(learningMetricDailyMapper, org.mockito.Mockito.times(2)).insertOrUpdate(captor.capture());
        List<LearningMetricDaily> metricList = captor.getAllValues();
        LearningMetricDaily mathMetric = metricList.stream()
                .filter(item -> "math".equals(item.getSubject())).findFirst().orElseThrow(AssertionError::new);
        assertEquals(2, mathMetric.getWrongQuestionCount());
        assertEquals(1, mathMetric.getMasteredCount());
        assertEquals(50, mathMetric.getMasteryRate());
        assertEquals(2, mathMetric.getReviewCount());
        assertEquals(2, mathMetric.getJudgedAnswerCount());
        assertEquals(1, mathMetric.getCorrectAnswerCount());
        assertEquals(50, mathMetric.getRetentionRate());
    }

    private WrongQuestion wrongQuestion(Long userId, String subject, int status) {
        WrongQuestion question = new WrongQuestion();
        question.setCreateId(userId);
        question.setSubject(subject);
        question.setStatus(status);
        return question;
    }

    private ReviewRecord reviewRecord(Long userId, String subject, Integer isCorrect) {
        ReviewRecord record = new ReviewRecord();
        record.setUserId(userId);
        record.setSubject(subject);
        record.setReviewTime(LocalDateTime.now());
        record.setIsCorrect(isCorrect);
        record.setIsIndependent(1);
        return record;
    }
}
