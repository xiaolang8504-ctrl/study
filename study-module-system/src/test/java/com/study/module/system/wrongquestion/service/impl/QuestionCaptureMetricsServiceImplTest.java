package com.study.module.system.wrongquestion.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 题目采集运行指标服务测试。
 */
class QuestionCaptureMetricsServiceImplTest {

    /**
     * OCR 与超时恢复结果应写入对应指标。
     */
    @Test
    void shouldRecordCaptureMetrics() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        QuestionCaptureMetricsServiceImpl service = new QuestionCaptureMetricsServiceImpl();
        ReflectionTestUtils.setField(service, "meterRegistry", meterRegistry);

        service.recordOcrPageResult(true, 25L);
        service.recordOcrPageResult(false, 30L, "PADDLE_OCR", "PP-StructureV3", "数学");
        service.recordCleanImageResult(true, 12L);
        service.recordRecoveryResult(false);

        assertEquals(1D, meterRegistry.get("question.capture.ocr.pages")
                .tag("result", "success").counter().count());
        assertEquals(1D, meterRegistry.get("question.capture.recovery.tasks")
                .tag("result", "failed").counter().count());
        assertEquals(1D, meterRegistry.get("question.capture.ocr.pages")
                .tags("result", "failed", "provider", "PADDLE_OCR", "model", "PP-StructureV3", "subject", "数学")
                .counter().count());
        assertEquals(1D, meterRegistry.get("question.capture.clean.pages")
                .tag("result", "success").counter().count());
    }
}
