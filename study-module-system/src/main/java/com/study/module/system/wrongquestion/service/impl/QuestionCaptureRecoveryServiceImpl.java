package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureMetricsService;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureRecoveryService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目采集超时恢复服务实现。
 */
@Slf4j
@Service
public class QuestionCaptureRecoveryServiceImpl implements QuestionCaptureRecoveryService {

    private static final int TASK_WAITING = 0;
    private static final int TASK_PROCESSING = 1;
    private static final int TASK_FAILED = 4;
    private static final int PAGE_WAITING = 0;
    private static final int PAGE_PROCESSING = 1;
    private static final int MAX_AUTO_RETRY_COUNT = 3;

    @Autowired
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired
    private QuestionCapturePageService questionCapturePageService;
    @Autowired
    private QuestionCaptureOcrService questionCaptureOcrService;
    @Autowired
    private QuestionCaptureMetricsService questionCaptureMetricsService;
    @Autowired
    private QuestionCaptureEventService questionCaptureEventService;

    /**
     * 定期处理超过阈值仍处于排队或处理中的任务。
     */
    @Override
    @Scheduled(fixedDelayString = "${wrong-question.capture.recovery-delay:60000}")
    @Transactional(rollbackFor = Exception.class)
    public void recoverTimeoutQuestionCaptureTasks() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(30);
        List<QuestionCaptureTask> timeoutTasks = questionCaptureTaskService.list(
                new LambdaQueryWrapper<QuestionCaptureTask>()
                        .in(QuestionCaptureTask::getStatus, TASK_WAITING, TASK_PROCESSING)
                        .lt(QuestionCaptureTask::getUpdateTime, timeoutTime));
        for (QuestionCaptureTask task : timeoutTasks) {
            recoverTimeoutTask(task);
        }
    }

    /**
     * 页面已落库的任务可安全重入队；PDF 尚未落页的任务改为失败并提示用户重试。
     */
    private void recoverTimeoutTask(QuestionCaptureTask task) {
        // 多实例定时任务通过状态和更新时间抢占同一个超时任务，只有获胜实例重入队。
        boolean claimed = questionCaptureTaskService.update(new UpdateWrapper<QuestionCaptureTask>()
                .eq("id", task.getId())
                .eq("status", task.getStatus())
                .eq("update_time", task.getUpdateTime())
                .set("update_time", LocalDateTime.now()));
        if (!claimed) {
            return;
        }
        List<QuestionCapturePage> pendingPages = questionCapturePageService.list(
                new LambdaQueryWrapper<QuestionCapturePage>().eq(QuestionCapturePage::getTaskId, task.getId())
                        .in(QuestionCapturePage::getStatus, PAGE_WAITING, PAGE_PROCESSING));
        LocalDateTime now = LocalDateTime.now();
        if (pendingPages.isEmpty() || defaultValue(task.getRetryCount()) >= MAX_AUTO_RETRY_COUNT) {
            task.setStatus(TASK_FAILED);
            task.setFailReason(pendingPages.isEmpty() ? "任务处理超时，请重新上传或重试"
                    : "任务自动重试超过3次，请手动重试");
            task.setUpdateTime(now);
            questionCaptureTaskService.updateById(task);
            questionCaptureMetricsService.recordRecoveryResult(false);
            questionCaptureEventService.record(task.getId(), null, null, task.getCreateId(), null,
                    "TASK_RECOVERY", "FAILED", null, null, null, null, task.getFailReason());
            log.warn("题目采集任务超时且没有可恢复页面，taskId={}", task.getId());
            return;
        }
        for (QuestionCapturePage page : pendingPages) {
            page.setStatus(PAGE_WAITING);
            page.setFailReason(null);
            page.setUpdateTime(now);
            questionCapturePageService.updateById(page);
        }
        task.setStatus(TASK_WAITING);
        task.setFailReason(null);
        task.setRetryCount(defaultValue(task.getRetryCount()) + 1);
        task.setUpdateTime(now);
        questionCaptureTaskService.updateById(task);
        questionCaptureMetricsService.recordRecoveryResult(true);
        questionCaptureEventService.record(task.getId(), null, null, task.getCreateId(), null,
                "TASK_RECOVERY", "SUCCESS", null, null, pendingPages.size(), null, null);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    questionCaptureOcrService.recognizeQuestionCaptureTask(task.getId());
                }
            });
        } else {
            questionCaptureOcrService.recognizeQuestionCaptureTask(task.getId());
        }
        log.info("题目采集任务已重新入队，taskId={}", task.getId());
    }

    /**
     * 返回默认重试次数。
     */
    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}
