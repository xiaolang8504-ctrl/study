package com.study.module.system.wrongquestion.service.impl;

import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureMetricsService;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 题目采集超时恢复服务测试。
 */
@ExtendWith(MockitoExtension.class)
class QuestionCaptureRecoveryServiceImplTest {

    @Mock
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Mock
    private QuestionCapturePageService questionCapturePageService;
    @Mock
    private QuestionCaptureOcrService questionCaptureOcrService;
    @Mock
    private QuestionCaptureMetricsService questionCaptureMetricsService;
    @Mock
    private QuestionCaptureEventService questionCaptureEventService;
    @InjectMocks
    private QuestionCaptureRecoveryServiceImpl questionCaptureRecoveryService;

    /**
     * 已落库的超时页面应重置为待处理并重新提交 OCR。
     */
    @Test
    void shouldRequeueTimeoutTaskWithPendingPages() {
        QuestionCaptureTask task = timeoutTask(1L);
        QuestionCapturePage page = new QuestionCapturePage();
        page.setId(11L);
        page.setStatus(1);
        when(questionCaptureTaskService.list(org.mockito.ArgumentMatchers.<Wrapper<QuestionCaptureTask>>any()))
                .thenReturn(Collections.singletonList(task));
        when(questionCaptureTaskService.update(org.mockito.ArgumentMatchers.<Wrapper<QuestionCaptureTask>>any()))
                .thenReturn(true);
        when(questionCapturePageService.list(org.mockito.ArgumentMatchers.<Wrapper<QuestionCapturePage>>any()))
                .thenReturn(Collections.singletonList(page));

        questionCaptureRecoveryService.recoverTimeoutQuestionCaptureTasks();

        verify(questionCapturePageService).updateById(page);
        verify(questionCaptureTaskService).updateById(task);
        verify(questionCaptureOcrService).recognizeQuestionCaptureTask(1L);
        verify(questionCaptureMetricsService).recordRecoveryResult(true);
    }

    /**
     * 没有可恢复页面的超时任务必须失败，不能盲目重复 OCR。
     */
    @Test
    void shouldFailTimeoutTaskWithoutPendingPages() {
        QuestionCaptureTask task = timeoutTask(2L);
        when(questionCaptureTaskService.list(org.mockito.ArgumentMatchers.<Wrapper<QuestionCaptureTask>>any()))
                .thenReturn(Collections.singletonList(task));
        when(questionCaptureTaskService.update(org.mockito.ArgumentMatchers.<Wrapper<QuestionCaptureTask>>any()))
                .thenReturn(true);
        when(questionCapturePageService.list(org.mockito.ArgumentMatchers.<Wrapper<QuestionCapturePage>>any()))
                .thenReturn(Collections.emptyList());

        questionCaptureRecoveryService.recoverTimeoutQuestionCaptureTasks();

        verify(questionCaptureTaskService).updateById(task);
        verify(questionCaptureOcrService, never()).recognizeQuestionCaptureTask(any());
        verify(questionCaptureMetricsService).recordRecoveryResult(false);
    }

    @Test
    void shouldNotDispatchTaskClaimedByAnotherInstance() {
        QuestionCaptureTask task = timeoutTask(3L);
        when(questionCaptureTaskService.list(org.mockito.ArgumentMatchers.<Wrapper<QuestionCaptureTask>>any()))
                .thenReturn(Collections.singletonList(task));
        when(questionCaptureTaskService.update(org.mockito.ArgumentMatchers.<Wrapper<QuestionCaptureTask>>any()))
                .thenReturn(false);

        questionCaptureRecoveryService.recoverTimeoutQuestionCaptureTasks();

        verify(questionCapturePageService, never()).list(org.mockito.ArgumentMatchers.<Wrapper<QuestionCapturePage>>any());
        verify(questionCaptureOcrService, never()).recognizeQuestionCaptureTask(any());
    }

    /**
     * 创建满足超时筛选条件的任务数据。
     */
    private QuestionCaptureTask timeoutTask(Long id) {
        QuestionCaptureTask task = new QuestionCaptureTask();
        task.setId(id);
        task.setStatus(1);
        task.setUpdateTime(LocalDateTime.now().minusHours(1));
        return task;
    }
}
