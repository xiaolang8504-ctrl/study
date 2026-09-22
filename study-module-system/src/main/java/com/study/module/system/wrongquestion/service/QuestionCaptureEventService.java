package com.study.module.system.wrongquestion.service;

/**
 * 题目采集可观测事件服务。
 */
public interface QuestionCaptureEventService {

    /**
     * 写入采集过程中的结构化事件。事件写入失败不得中断主学习链路。
     */
    void record(Long taskId, Long pageId, Long regionId, Long userId, Long fileId, String eventType,
                String result, String ocrProvider, String ocrModel, Integer regionCount,
                Long elapsedMillis, String errorMessage);
}
