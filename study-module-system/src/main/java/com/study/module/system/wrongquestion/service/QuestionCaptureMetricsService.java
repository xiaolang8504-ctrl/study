package com.study.module.system.wrongquestion.service;

/**
 * 题目采集运行指标服务。
 */
public interface QuestionCaptureMetricsService {

    /**
     * 记录 OCR 页面处理结果。
     */
    void recordOcrPageResult(boolean success, long elapsedMillis);

    /**
     * 记录 OCR 页面结果，并附带有限枚举维度以支撑供应商、模型、学科分组告警。
     */
    void recordOcrPageResult(boolean success, long elapsedMillis, String provider, String model, String subject);

    /**
     * 记录本地净化图生成结果，净化失败不会阻断 OCR 和错题确认。
     */
    void recordCleanImageResult(boolean success, long elapsedMillis);

    /**
     * 记录采集任务超时恢复结果。
     */
    void recordRecoveryResult(boolean recovered);
}
