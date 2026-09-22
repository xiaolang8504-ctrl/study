package com.study.module.system.wrongquestion.service;
/**
 * 题目采集 OCR 识别服务
 */
public interface QuestionCaptureOcrService {

    /**
     * 识别采集任务中的全部页面。
     */
    void recognizeQuestionCaptureTask(Long taskId);

    /**
     * 识别指定采集页面。
     */
    void recognizeQuestionCapturePage(Long pageId);
}
