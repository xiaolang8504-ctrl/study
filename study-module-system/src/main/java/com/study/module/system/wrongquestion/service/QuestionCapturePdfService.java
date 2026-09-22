package com.study.module.system.wrongquestion.service;
import java.util.List;
/**
 * 题目采集 PDF 渲染服务
 */
public interface QuestionCapturePdfService {

    /**
     * 将任务中的 PDF 文件渲染为待识别图片。
     */
    void renderQuestionCapturePdfs(Long taskId, List<Long> fileIds);
}
