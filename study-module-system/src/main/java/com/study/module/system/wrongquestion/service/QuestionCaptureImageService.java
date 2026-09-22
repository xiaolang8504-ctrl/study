package com.study.module.system.wrongquestion.service;
/**
 * 题目采集图片处理服务
 */
public interface QuestionCaptureImageService {

    /**
     * 生成指定采集页的净化图片。
     */
    void generateCleanedImage(Long pageId);
}
