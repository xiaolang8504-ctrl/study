package com.study.module.system.review.service;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
public interface PracticePaperDocumentService {
    byte[] renderPdf(PracticeSessionDetailResp paper, boolean answerMode);
    byte[] renderDocx(PracticeSessionDetailResp paper, boolean answerMode);
}
