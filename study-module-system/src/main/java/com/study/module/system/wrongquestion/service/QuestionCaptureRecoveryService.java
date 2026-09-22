package com.study.module.system.wrongquestion.service;

/**
 * 题目采集超时恢复服务。
 */
public interface QuestionCaptureRecoveryService {

    /**
     * 恢复超时未完成的题目采集任务。
     */
    void recoverTimeoutQuestionCaptureTasks();
}
