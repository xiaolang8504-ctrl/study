package com.study.module.system.wrongquestion.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 采集前端和后端共识的文件类型约束测试。
 */
class QuestionCaptureFileTypeTest {

    @Test
    void shouldOnlyAcceptFilesSupportedByOcrAndCleanImagePipeline() {
        assertTrue(QuestionCaptureFileType.isSupported("JPG"));
        assertTrue(QuestionCaptureFileType.isSupported("jpeg"));
        assertTrue(QuestionCaptureFileType.isSupported("png"));
        assertTrue(QuestionCaptureFileType.isSupported("pdf"));
        assertFalse(QuestionCaptureFileType.isSupported("webp"));
        assertFalse(QuestionCaptureFileType.isSupported("docx"));
        assertFalse(QuestionCaptureFileType.isSupported("gif"));
    }
}
