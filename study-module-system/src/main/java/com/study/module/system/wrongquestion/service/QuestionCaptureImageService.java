package com.study.module.system.wrongquestion.service;
/**
 * 题目采集图片处理服务
 */
public interface QuestionCaptureImageService {

    /** 生成灰度预览和真正的去笔迹图片；两者分别保存。 */
    void generateCleanedImage(Long pageId);

    /** 回退到原图，同时保留既有清理版本记录，便于审计和再次处理。 */
    void revertCleanedImage(Long pageId);

    /**
     * 将学生上传的整页人工遮罩图设为当前清理版本；原始采集图始终保留，不可被该操作覆盖。
     */
    void applyManualCleanImage(Long pageId, Long cleanedFileId);
}
