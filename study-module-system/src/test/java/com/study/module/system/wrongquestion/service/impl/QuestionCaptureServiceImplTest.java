package com.study.module.system.wrongquestion.service.impl;

import com.study.api.provider.FileProvider;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.service.FileService;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCapturePdfService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 题目采集任务的权限边界测试。
 */
@ExtendWith(MockitoExtension.class)
class QuestionCaptureServiceImplTest {

    @Mock
    private FileService fileService;
    @Mock
    private FileProvider fileProvider;
    @Mock
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Mock
    private QuestionCapturePageService questionCapturePageService;
    @Mock
    private QuestionCapturePdfService questionCapturePdfService;
    @Mock
    private QuestionCaptureOcrService questionCaptureOcrService;
    @Mock
    private QuestionCaptureEventService questionCaptureEventService;
    @InjectMocks
    private QuestionCaptureServiceImpl questionCaptureService;

    /**
     * 不能通过猜测文件 ID 将其他学生的私有文件挂入自己的采集任务。
     */
    @Test
    void shouldRejectCaptureTaskWhenSourceFileDoesNotBelongToCurrentStudent() {
        QuestionCaptureTaskCreateReq request = new QuestionCaptureTaskCreateReq();
        request.setImageFileIds(Collections.singletonList(101L));
        when(fileService.checkUserFile(eq(101), any(String.class), eq(7L)))
                .thenThrow(new LogicException(ErrorCodeConstants.ACCESS_DENIED));

        assertThrows(LogicException.class,
                () -> questionCaptureService.createQuestionCaptureTaskByUserId(request, 7L));

        verify(questionCaptureTaskService, never()).save(any());
    }

    /**
     * PDF 分页失败时应重新提交分页，而不是将 PDF 占位页送入 OCR 或直接失败。
     */
    @Test
    void shouldRetryPdfRenderingForOwnedPlaceholderPage() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(7L, null));
        QuestionCapturePage page = new QuestionCapturePage();
        page.setId(11L);
        page.setSourceFileId(101L);
        page.setImageFileId(101L);
        QuestionCaptureTask task = new QuestionCaptureTask();
        task.setId(1L);
        task.setCreateId(7L);
        task.setStatus(4);
        when(questionCapturePageService.getById(11L)).thenReturn(page);
        when(questionCaptureTaskService.getOne(org.mockito.ArgumentMatchers.any())).thenReturn(task);

        questionCaptureService.retryQuestionCapturePage(11L);

        verify(questionCapturePageService).removeById(11L);
        verify(questionCaptureTaskService).updateById(task);
        verify(questionCapturePdfService).renderQuestionCapturePdfs(1L, Collections.singletonList(101L));
        verify(questionCaptureOcrService, never()).recognizeQuestionCapturePage(any());
    }

    /**
     * 即使知道页面编号，其他学生也不能触发该采集任务的恢复操作。
     */
    @Test
    void shouldRejectRetryWhenCaptureTaskDoesNotBelongToCurrentStudent() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(7L, null));
        QuestionCapturePage page = new QuestionCapturePage();
        page.setId(11L);
        page.setTaskId(1L);
        when(questionCapturePageService.getById(11L)).thenReturn(page);
        when(questionCaptureTaskService.getOne(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        assertThrows(LogicException.class, () -> questionCaptureService.retryQuestionCapturePage(11L));

        verify(questionCapturePageService, never()).removeById(11L);
        verify(questionCapturePdfService, never()).renderQuestionCapturePdfs(any(), any());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
