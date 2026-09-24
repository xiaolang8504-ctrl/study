package com.study.module.system.wrongquestion.service;

import java.util.List;

/** 将 DOCX 或网页粘贴内容转换为采集工作台的一页待确认题块。 */
public interface QuestionCaptureDocumentService {
    void importQuestionCaptureDocuments(Long taskId, List<Long> docxFileIds, String documentContent, String documentTitle);
}
