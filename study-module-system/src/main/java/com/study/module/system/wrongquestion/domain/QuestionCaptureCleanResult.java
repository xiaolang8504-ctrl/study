package com.study.module.system.wrongquestion.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.image.BufferedImage;

/** 单个卷面清理提供方的输出，不包含任何原图覆盖语义。 */
@Getter
@AllArgsConstructor
public class QuestionCaptureCleanResult {
    private final BufferedImage image;
    /** 已安全去除像素的比例，范围 0-1。 */
    private final double removedRatio;
}
