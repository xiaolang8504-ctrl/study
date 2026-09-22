package com.study.module.system.questionbank.service;

import com.study.module.system.questionbank.dto.request.QuestionBankReviewReq;
import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionBankReviewHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionBankVersionListResp;

import java.util.List;

/**
 * 题目版本与审核历史服务。
 */
public interface QuestionBankHistoryService {
    void createVersion(Long questionId, QuestionBankSaveReq request, String operationType);
    void createReviewLog(QuestionBankReviewReq request, Long reviewerId);
    List<QuestionBankVersionListResp> questionBankVersionList(Long questionId);
    List<QuestionBankReviewHistoryResp> questionBankReviewHistory(Long questionId);
}
