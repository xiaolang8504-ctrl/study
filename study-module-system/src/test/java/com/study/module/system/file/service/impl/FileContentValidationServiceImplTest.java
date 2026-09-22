package com.study.module.system.file.service.impl;

import com.study.common.core.exception.LogicException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 文件魔数校验测试。
 */
class FileContentValidationServiceImplTest {

    private final FileContentValidationServiceImpl service = new FileContentValidationServiceImpl();

    @Test
    void shouldAcceptMatchingPngContent() {
        assertDoesNotThrow(() -> service.validate("png", new ByteArrayInputStream(new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        })));
    }

    @Test
    void shouldRejectExtensionSpoofing() {
        assertThrows(LogicException.class, () -> service.validate("pdf", new ByteArrayInputStream(new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        })));
    }

    @Test
    void shouldAcceptOfficeZipPackage() {
        assertDoesNotThrow(() -> service.validate("docx", new ByteArrayInputStream(new byte[]{
                0x50, 0x4B, 0x03, 0x04
        })));
    }
}
