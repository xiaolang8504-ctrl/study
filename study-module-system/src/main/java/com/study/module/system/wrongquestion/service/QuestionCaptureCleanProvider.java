package com.study.module.system.wrongquestion.service;

import com.study.module.system.wrongquestion.domain.QuestionCaptureCleanResult;

import java.awt.image.BufferedImage;

/**
 * 卷面清理适配层。替换为外部服务时只需新增实现，不改变页面、版本和原图回退链路。
 */
public interface QuestionCaptureCleanProvider {

    String providerCode();

    String algorithmVersion();

    QuestionCaptureCleanResult clean(BufferedImage source);
}
