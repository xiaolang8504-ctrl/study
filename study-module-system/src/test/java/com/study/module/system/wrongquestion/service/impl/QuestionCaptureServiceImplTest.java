package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.api.provider.FileProvider;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionBatchUpdateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionUpdateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCapturePdfService;
import com.study.module.system.wrongquestion.service.QuestionCaptureRegionService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private QuestionCaptureRegionService questionCaptureRegionService;
    @Mock
    private QuestionCapturePdfService questionCapturePdfService;
    @Mock
    private QuestionCaptureOcrService questionCaptureOcrService;
    @Mock
    private QuestionCaptureEventService questionCaptureEventService;
    @Mock
    private DictDataService dictDataService;
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

    /**
     * 工作台既可能提交字典键，也可能提交历史标签，保存题块时都应归一化为字典键。
     */
    @Test
    void shouldNormalizeRegionClassificationWhenUpdatingPendingRegion() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(7L, null));
        QuestionCaptureRegion region = new QuestionCaptureRegion();
        region.setId(21L);
        region.setTaskId(1L);
        region.setPageId(11L);
        region.setStatus(0);
        QuestionCaptureTask task = new QuestionCaptureTask();
        task.setId(1L);
        task.setCreateId(7L);
        task.setStatus(2);
        when(questionCaptureRegionService.getById(21L)).thenReturn(region);
        when(questionCaptureTaskService.getOne(any())).thenReturn(task);
        when(questionCaptureRegionService.updateById(region)).thenReturn(true);
        when(dictDataService.dictDataIdMap("grade")).thenReturn(Collections.singletonMap("grade_1", "一年级"));
        when(dictDataService.dictDataIdMap("subject")).thenReturn(Collections.singletonMap("math", "数学"));
        when(dictDataService.dictDataIdMap("question_type")).thenReturn(Collections.singletonMap("choice", "选择题"));
        when(dictDataService.dictDataIdMap("source")).thenReturn(Collections.singletonMap("exam", "考试"));
        QuestionCaptureRegionUpdateReq request = new QuestionCaptureRegionUpdateReq();
        request.setId(21L);
        request.setGrade("一年级");
        request.setSubject("math");
        request.setQuestionType("选择题");
        request.setSource("exam");

        questionCaptureService.updateQuestionCaptureRegion(request);

        assertEquals("grade_1", region.getGrade());
        assertEquals("math", region.getSubject());
        assertEquals("choice", region.getQuestionType());
        assertEquals("exam", region.getSource());
        assertEquals(1, region.getManuallyCorrected());
    }

    /**
     * 批量归类应在归属校验后统一更新全部待确认题块。
     */
    @Test
    void shouldBatchUpdateClassificationForPendingRegions() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(7L, null));
        QuestionCaptureTask task = new QuestionCaptureTask();
        task.setId(1L);
        task.setCreateId(7L);
        task.setStatus(2);
        QuestionCaptureRegion first = new QuestionCaptureRegion();
        first.setId(21L);
        first.setTaskId(1L);
        first.setStatus(0);
        QuestionCaptureRegion second = new QuestionCaptureRegion();
        second.setId(22L);
        second.setTaskId(1L);
        second.setStatus(0);
        when(questionCaptureTaskService.getOne(any())).thenReturn(task);
        when(questionCaptureRegionService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(first, second));
        when(questionCaptureRegionService.updateById(any())).thenReturn(true);
        when(dictDataService.dictDataIdMap("grade")).thenReturn(Collections.singletonMap("grade_1", "一年级"));
        QuestionCaptureRegionBatchUpdateReq request = new QuestionCaptureRegionBatchUpdateReq();
        request.setTaskId(1L);
        request.setRegionIds(Arrays.asList(21L, 22L));
        request.setGrade("一年级");

        questionCaptureService.batchUpdateQuestionCaptureRegion(request);

        assertEquals("grade_1", first.getGrade());
        assertEquals("grade_1", second.getGrade());
        assertEquals(1, first.getManuallyCorrected());
        assertEquals(1, second.getManuallyCorrected());
        verify(questionCaptureRegionService, times(2)).updateById(any());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
