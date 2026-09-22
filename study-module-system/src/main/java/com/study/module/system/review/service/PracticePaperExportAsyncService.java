package com.study.module.system.review.service;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
public interface PracticePaperExportAsyncService {
    void generatePracticePaper(Long taskId, Long userId, PracticeSessionDetailResp paper, String format, Integer answerMode);
}
