package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.study.module.system.wrongquestion.domain.WrongQuestionOcrData;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureImageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureMetricsService;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureRegionService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.study.module.system.wrongquestion.service.WrongQuestionImageOcrService;
import com.study.module.system.wrongquestion.config.WrongQuestionOcrConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在独立线程中执行 OCR，避免占用创建采集任务的 HTTP 请求线程。
 */
@Service
@Slf4j
public class QuestionCaptureOcrServiceImpl implements QuestionCaptureOcrService {

    @Autowired
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired
    private QuestionCapturePageService questionCapturePageService;
    @Autowired
    private QuestionCaptureRegionService questionCaptureRegionService;
    @Autowired
    private WrongQuestionImageOcrService wrongQuestionImageOcrService;
    @Autowired
    private QuestionCaptureImageService questionCaptureImageService;
    @Autowired
    private QuestionCaptureMetricsService questionCaptureMetricsService;
    @Autowired
    private QuestionCaptureEventService questionCaptureEventService;
    @Autowired
    private WrongQuestionOcrConfig wrongQuestionOcrConfig;

    /**
     * 处理题目采集。
     */
    @Override
    @Async("questionCaptureExecutor")
    public void recognizeQuestionCaptureTask(Long taskId) {
        updateTaskStatus(taskId, 1, null);
        List<QuestionCapturePage> pages = questionCapturePageService.list(new LambdaQueryWrapper<QuestionCapturePage>()
                .eq(QuestionCapturePage::getTaskId, taskId)
                .in(QuestionCapturePage::getStatus, 0, 1)
                .orderByAsc(QuestionCapturePage::getPageNo));
        for (QuestionCapturePage page : pages) {
            try {
                recognizePage(page);
            } catch (Exception e) {
                markPageFailed(page, e);
            }
        }
        refreshTaskStatus(taskId);
    }

    /**
     * 处理题目采集。
     */
    @Override
    @Async("questionCaptureExecutor")
    public void recognizeQuestionCapturePage(Long pageId) {
        QuestionCapturePage page = questionCapturePageService.getById(pageId);
        if (page == null) {
            return;
        }
        try {
            recognizePage(page);
            refreshTaskStatus(page.getTaskId());
        } catch (Exception e) {
            markPageFailed(page, e);
            refreshTaskStatus(page.getTaskId());
        }
    }

    /**
     * 保存 OCR 题块时保留已确认题块，编号从该页最大编号继续递增。
     */
    private void recognizePage(QuestionCapturePage page) {
        long startTime = System.currentTimeMillis();
        QuestionCaptureTask task = questionCaptureTaskService.getById(page.getTaskId());
        if (task == null || task.getCreateId() == null) {
            throw new IllegalStateException("采集任务不存在或缺少归属用户");
        }
        page.setStatus(1);
        page.setFailReason(null);
        page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.updateById(page);
        List<WrongQuestionOcrData> dataList;
        try {
            dataList = wrongQuestionImageOcrService.recognizeLayout(page.getImageFileId(), task.getCreateId());
        } catch (RuntimeException e) {
            long elapsedMillis = System.currentTimeMillis() - startTime;
            questionCaptureMetricsService.recordOcrPageResult(false, elapsedMillis, "PADDLE_OCR",
                    wrongQuestionOcrConfig.getModel(), task.getSubject());
            questionCaptureEventService.record(page.getTaskId(), page.getId(), null, task.getCreateId(), page.getImageFileId(),
                    "OCR_PAGE", "FAILED", "PADDLE_OCR", wrongQuestionOcrConfig.getModel(), null, elapsedMillis,
                    e.getMessage());
            throw e;
        }
        int regionNo = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                        .eq(QuestionCaptureRegion::getPageId, page.getId()))
                .stream().map(QuestionCaptureRegion::getRegionNo).filter(item -> item != null).max(Integer::compareTo).orElse(0) + 1;
        LocalDateTime now = LocalDateTime.now();
        for (WrongQuestionOcrData data : dataList) {
            QuestionCaptureRegion region = new QuestionCaptureRegion();
            region.setTaskId(page.getTaskId());
            region.setPageId(page.getId());
            region.setRegionNo(regionNo++);
            region.setConfidence(defaultValue(data.getConfidence(), 50));
            region.setQuestionTitleConfidence(fieldConfidence(data.getQuestionTitle(), data.getConfidence()));
            region.setQuestionContentConfidence(fieldConfidence(data.getQuestionContent(), data.getConfidence()));
            region.setWrongAnswerConfidence(fieldConfidence(data.getWrongAnswer(), data.getConfidence()));
            region.setCorrectAnswerConfidence(fieldConfidence(data.getCorrectAnswer(), data.getConfidence()));
            region.setAnalysisConfidence(fieldConfidence(data.getAnalysis(), data.getConfidence()));
            region.setLeftPosition(defaultValue(data.getLeftPosition(), 0));
            region.setTopPosition(defaultValue(data.getTopPosition(), 0));
            region.setWidth(defaultValue(data.getWidth(), 10000));
            region.setHeight(defaultValue(data.getHeight(), 10000));
            region.setManuallyCorrected(0);
            region.setStatus(0);
            region.setQuestionTitle(data.getQuestionTitle());
            region.setQuestionContent(data.getQuestionContent());
            region.setWrongAnswer(data.getWrongAnswer());
            region.setCorrectAnswer(data.getCorrectAnswer());
            region.setWrongReason(data.getWrongReason());
            region.setAnalysis(data.getAnalysis());
            region.setCreateTime(now);
            region.setUpdateTime(now);
            questionCaptureRegionService.save(region);
        }
        page.setStatus(2);
        page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.updateById(page);
        questionCaptureImageService.generateCleanedImage(page.getId());
        long elapsedMillis = System.currentTimeMillis() - startTime;
        questionCaptureMetricsService.recordOcrPageResult(true, elapsedMillis, "PADDLE_OCR",
                wrongQuestionOcrConfig.getModel(), task.getSubject());
        questionCaptureEventService.record(page.getTaskId(), page.getId(), null, task.getCreateId(), page.getImageFileId(),
                "OCR_PAGE", "SUCCESS", "PADDLE_OCR", wrongQuestionOcrConfig.getModel(), dataList.size(),
                elapsedMillis, null);
    }

    /**
     * 显式写入失败原因；任务恢复成功时必须能将旧失败原因清空。
     */
    private void updateTaskStatus(Long taskId, Integer status, String failReason) {
        questionCaptureTaskService.update(new LambdaUpdateWrapper<QuestionCaptureTask>()
                .eq(QuestionCaptureTask::getId, taskId)
                .set(QuestionCaptureTask::getStatus, status)
                .set(QuestionCaptureTask::getFailReason, failReason)
                .set(QuestionCaptureTask::getUpdateTime, LocalDateTime.now()));
    }

    /**
     * 页面失败不再中断同一任务中其他页面的识别。
     */
    private void markPageFailed(QuestionCapturePage page, Exception e) {
        log.error("采集页面 OCR 失败，pageId={}", page.getId(), e);
        page.setStatus(4);
        page.setFailReason("OCR识别失败，请重试");
        page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.updateById(page);
    }

    /**
     * 按页面最终结果汇总任务状态，既保留成功题块，也明确提示仍需重试的失败页。
     */
    private void refreshTaskStatus(Long taskId) {
        List<QuestionCapturePage> pages = questionCapturePageService.list(new LambdaQueryWrapper<QuestionCapturePage>()
                .eq(QuestionCapturePage::getTaskId, taskId));
        long failedCount = pages.stream().filter(item -> Integer.valueOf(4).equals(item.getStatus())).count();
        long processingCount = pages.stream().filter(item -> Integer.valueOf(0).equals(item.getStatus()) || Integer.valueOf(1).equals(item.getStatus())).count();
        if (processingCount > 0) {
            updateTaskStatus(taskId, 1, null);
        } else if (failedCount > 0) {
            updateTaskStatus(taskId, 4, "有" + failedCount + "页识别失败，可逐页重试");
        } else {
            updateTaskStatus(taskId, 2, null);
        }
    }

    /**
     * 标准化并计算业务数据。
     */
    private int defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 当前 OCR 提供的是版面块置信度；先按字段是否成功提取拆分展示，后续模型可直接覆盖该字段值。
     */
    private int fieldConfidence(String value, Integer layoutConfidence) {
        return value == null || value.trim().isEmpty() ? 0 : defaultValue(layoutConfidence, 50);
    }
}
