package com.study.module.system.wrongquestion.service.impl;

import com.study.module.system.wrongquestion.service.QuestionCaptureMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 题目采集运行指标服务实现。
 */
@Service
public class QuestionCaptureMetricsServiceImpl implements QuestionCaptureMetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 记录 OCR 页面处理结果。
     */
    @Override
    public void recordOcrPageResult(boolean success, long elapsedMillis) {
        recordOcrPageResult(success, elapsedMillis, "unknown", "unknown", "unknown");
    }

    @Override
    public void recordOcrPageResult(boolean success, long elapsedMillis, String provider, String model, String subject) {
        String result = success ? "success" : "failed";
        String safeProvider = metricValue(provider);
        String safeModel = metricValue(model);
        String safeSubject = metricValue(subject);
        meterRegistry.counter("question.capture.ocr.pages", "result", result, "provider", safeProvider,
                "model", safeModel, "subject", safeSubject).increment();
        meterRegistry.timer("question.capture.ocr.duration", "result", result, "provider", safeProvider,
                "model", safeModel, "subject", safeSubject)
                .record(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordCleanImageResult(boolean success, long elapsedMillis) {
        String result = success ? "success" : "failed";
        meterRegistry.counter("question.capture.clean.pages", "result", result).increment();
        meterRegistry.timer("question.capture.clean.duration", "result", result)
                .record(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录采集任务超时恢复结果。
     */
    @Override
    public void recordRecoveryResult(boolean recovered) {
        meterRegistry.counter("question.capture.recovery.tasks", "result",
                recovered ? "requeued" : "failed").increment();
    }

    private String metricValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        return value.length() > 64 ? value.substring(0, 64) : value;
    }
}
