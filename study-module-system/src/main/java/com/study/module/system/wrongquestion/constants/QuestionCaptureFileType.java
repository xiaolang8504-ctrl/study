package com.study.module.system.wrongquestion.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 题目采集实际支持的源文件类型。
 *
 * <p>OCR 和净化图链路均按图片页面处理，因此只接受可稳定由当前服务处理的
 * JPEG、PNG 及 PDF。新增格式前必须同时验证前端预览、图片处理和 OCR 服务。</p>
 */
public final class QuestionCaptureFileType {

    public static final String PDF = "pdf";
    public static final String DOCX = "docx";

    private static final Set<String> SUPPORTED_EXTENSION_SET = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", PDF, DOCX
    ));

    private QuestionCaptureFileType() {
    }

    public static boolean isSupported(String extension) {
        return SUPPORTED_EXTENSION_SET.contains(normalize(extension));
    }

    public static boolean isPdf(String extension) {
        return PDF.equals(normalize(extension));
    }

    public static boolean isDocx(String extension) {
        return DOCX.equals(normalize(extension));
    }

    private static String normalize(String extension) {
        return extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
    }
}
