package com.study.module.system.file.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.service.FileContentValidationService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * 基于文件魔数的轻量级内容校验。
 *
 * <p>浏览器提交的 Content-Type 和文件扩展名均可伪造；上传入口至少对可识别的图片、PDF
 * 以及 Office 文件验证真实文件头。未知格式仍由上传类型白名单控制，避免破坏既有附件类型。</p>
 */
@Service
public class FileContentValidationServiceImpl implements FileContentValidationService {

    private static final int HEADER_LENGTH = 16;

    @Override
    public void validate(String extension, InputStream inputStream) {
        String normalizedExtension = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        if (!requiresSignatureValidation(normalizedExtension)) {
            return;
        }
        byte[] header = new byte[HEADER_LENGTH];
        int readLength;
        try {
            readLength = inputStream.read(header);
        } catch (IOException e) {
            throw new LogicException(ErrorCodeConstants.FILE_CONTENT_INVALID);
        }
        if (!matches(normalizedExtension, header, readLength)) {
            throw new LogicException(ErrorCodeConstants.FILE_CONTENT_INVALID);
        }
    }

    private boolean requiresSignatureValidation(String extension) {
        return "pdf".equals(extension) || "png".equals(extension) || "jpg".equals(extension)
                || "jpeg".equals(extension) || "gif".equals(extension) || "webp".equals(extension)
                || "doc".equals(extension) || "xls".equals(extension) || "ppt".equals(extension)
                || "docx".equals(extension) || "xlsx".equals(extension) || "pptx".equals(extension);
    }

    private boolean matches(String extension, byte[] header, int length) {
        if ("pdf".equals(extension)) {
            return startsWith(header, length, 0x25, 0x50, 0x44, 0x46, 0x2D);
        }
        if ("png".equals(extension)) {
            return startsWith(header, length, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
        }
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return startsWith(header, length, 0xFF, 0xD8, 0xFF);
        }
        if ("gif".equals(extension)) {
            return startsWith(header, length, 0x47, 0x49, 0x46, 0x38);
        }
        if ("webp".equals(extension)) {
            return startsWith(header, length, 0x52, 0x49, 0x46, 0x46)
                    && hasBytesAt(header, length, 8, 0x57, 0x45, 0x42, 0x50);
        }
        if ("doc".equals(extension) || "xls".equals(extension) || "ppt".equals(extension)) {
            return startsWith(header, length, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
        }
        return startsWith(header, length, 0x50, 0x4B, 0x03, 0x04)
                || startsWith(header, length, 0x50, 0x4B, 0x05, 0x06)
                || startsWith(header, length, 0x50, 0x4B, 0x07, 0x08);
    }

    private boolean startsWith(byte[] header, int length, int... expected) {
        return hasBytesAt(header, length, 0, expected);
    }

    private boolean hasBytesAt(byte[] header, int length, int offset, int... expected) {
        if (length < offset + expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if ((header[offset + index] & 0xFF) != expected[index]) {
                return false;
            }
        }
        return true;
    }
}
